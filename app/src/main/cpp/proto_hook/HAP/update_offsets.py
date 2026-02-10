#!/usr/bin/env python3
"""
HAP64 Offset Auto-Updater

Parses HAP64.cpp comment annotations (@class, @field, @method, @slot, @position,
@generic, @deprecated) and cross-references them against a dump.cs (Il2CppDumper
output) to automatically update hex offsets.

Usage:
    python3 update_offsets.py                    # default files in same dir
    python3 update_offsets.py --dry-run          # preview without writing
    python3 update_offsets.py --dump new_dump.cs # custom dump file
"""

import argparse
import os
import re
import sys
from collections import defaultdict
from dataclasses import dataclass, field
from typing import Dict, List, Optional, Tuple


# ─── Data classes ────────────────────────────────────────────────────────────

@dataclass
class HapEntry:
    """A single entry parsed from HAP64.cpp."""
    cpp_func: str               # e.g. "pP_gIAo"
    line_num: int               # line of "return 0x...;"
    current_offset: str         # e.g. "0x70"
    entry_type: str             # "field" | "method" | "deprecated" | "unknown"
    class_name: str = ""        # extracted from @class
    class_line: str = ""        # full @class line
    # Field-specific
    field_name: str = ""        # e.g. "individualAttack_"
    field_type: str = ""        # e.g. "int"
    field_line: str = ""        # full @field line
    # Method-specific
    method_name: str = ""       # e.g. "bsob" or "GetPokemon"
    method_sig: str = ""        # full signature line
    return_type: str = ""       # e.g. "float", "void"
    param_types: List[str] = field(default_factory=list)
    access: str = ""            # e.g. "private", "public"
    modifiers: List[str] = field(default_factory=list)  # "virtual", "override", "static", "sealed"
    # Disambiguation
    slot: Optional[int] = None
    position: Optional[int] = None
    generic_key: str = ""       # e.g. "RpcHandler.SendRpc<object, object>"
    comment_hint: str = ""      # @comment value
    # Raw
    comment_block: str = ""
    return_line: str = ""       # full "    return 0x...;" line


@dataclass
class DumpMethod:
    """A method parsed from dump.cs."""
    name: str
    rva: str                    # e.g. "0xA5B32C4" or "-1"
    return_type: str
    param_types: List[str]
    access: str
    modifiers: List[str]
    slot: Optional[int] = None
    line_num: int = 0
    position_in_class: int = 0  # 0-indexed position among all methods in class
    raw_line: str = ""


@dataclass
class DumpField:
    """A field parsed from dump.cs."""
    name: str
    field_type: str
    offset: str                 # e.g. "0x70"
    line_num: int = 0
    raw_line: str = ""


@dataclass
class DumpClass:
    """A class parsed from dump.cs."""
    name: str
    full_line: str
    methods: List[DumpMethod] = field(default_factory=list)
    fields: List[DumpField] = field(default_factory=list)
    generic_inst: Dict[str, List[str]] = field(default_factory=dict)  # key -> [rva, ...]


# ─── Colors ──────────────────────────────────────────────────────────────────

class C:
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    RED = "\033[91m"
    CYAN = "\033[96m"
    DIM = "\033[2m"
    BOLD = "\033[1m"
    RESET = "\033[0m"

    @staticmethod
    def disable():
        C.GREEN = C.YELLOW = C.RED = C.CYAN = C.DIM = C.BOLD = C.RESET = ""


# ─── HAP64.cpp Parser ───────────────────────────────────────────────────────

def parse_method_signature(sig: str) -> Tuple[str, str, str, List[str], List[str]]:
    """
    Parse a C# method signature like:
      'private void bdbp() { }'
      'public override void Initialize(IMapPlace newMapPlace) { }'
      'public IPromise<OpenGiftOutProto> OpenGift(ulong giftboxId, string s) { }'
    Returns: (access, modifiers, return_type, method_name, param_types)
    """
    sig = sig.strip()
    # Remove trailing ' { }' or ' { }' or ';'
    sig = re.sub(r'\s*\{[^}]*\}\s*;?\s*$', '', sig)
    sig = re.sub(r'\s*;\s*$', '', sig)
    sig = sig.strip()

    # Split into parts before and after parentheses
    paren_match = re.search(r'\(([^)]*)\)\s*$', sig)
    if paren_match:
        params_str = paren_match.group(1).strip()
        before_parens = sig[:paren_match.start()].strip()
    else:
        params_str = ""
        before_parens = sig

    # Parse parameter types (ignore names and default values)
    param_types = []
    if params_str:
        for p in split_params(params_str):
            p = p.strip()
            # Remove default values like "= 0", "= True", "= False"
            p = re.sub(r'\s*=\s*[^,]+$', '', p).strip()
            parts = p.rsplit(None, 1)
            if len(parts) >= 2:
                param_types.append(parts[0].strip())
            elif len(parts) == 1:
                param_types.append(parts[0].strip())

    # Parse the part before parentheses: access modifiers return_type name
    tokens = before_parens.split()
    access = ""
    modifiers = []
    return_type = ""
    method_name = ""

    access_keywords = {"public", "private", "protected", "internal"}
    modifier_keywords = {"static", "virtual", "override", "abstract", "sealed", "readonly", "new", "extern"}

    i = 0
    # Access modifier
    if i < len(tokens) and tokens[i] in access_keywords:
        access = tokens[i]
        i += 1

    # Additional modifiers
    while i < len(tokens) and tokens[i] in modifier_keywords:
        modifiers.append(tokens[i])
        i += 1

    # Remaining: return_type name (or just name if constructor)
    remaining = tokens[i:]
    if len(remaining) >= 2:
        # Everything except last token is return type, last is name
        method_name = remaining[-1]
        return_type = " ".join(remaining[:-1])
    elif len(remaining) == 1:
        method_name = remaining[0]
        return_type = ""
    
    return access, modifiers, return_type, method_name, param_types


def split_params(s: str) -> List[str]:
    """Split parameter list respecting generic angle brackets."""
    params = []
    depth = 0
    current = []
    for ch in s:
        if ch == '<':
            depth += 1
            current.append(ch)
        elif ch == '>':
            depth -= 1
            current.append(ch)
        elif ch == ',' and depth == 0:
            params.append(''.join(current))
            current = []
        else:
            current.append(ch)
    if current:
        params.append(''.join(current))
    return params


def extract_class_name(class_line: str) -> str:
    """Extract the simple class name from a @class line."""
    # e.g. "public sealed class PokemonProto : IFoo, IBar // TypeDefIndex: 123"
    # Match the class/struct name after 'class' or 'struct'
    m = re.search(r'\bclass\s+(\S+)', class_line)
    if m:
        name = m.group(1)
        # Remove generic params like <T>
        name = re.sub(r'<.*$', '', name)
        # Remove trailing colon if present
        name = name.rstrip(':')
        return name
    return ""


def parse_hap64(filepath: str) -> List[HapEntry]:
    """Parse HAP64.cpp and return a list of HapEntry objects."""
    with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
        lines = f.readlines()

    entries = []
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Look for 'return 0x' pattern
        ret_match = re.match(r'\s*return\s+(0x[0-9a-fA-F]+)\s*;', line)
        if not ret_match:
            i += 1
            continue

        offset = ret_match.group(1)
        return_line_num = i + 1  # 1-indexed

        # Find the function name on the line above
        func_name = ""
        if i >= 1:
            func_match = re.search(r'HAP64::(\w+)\s*\(\s*\)', lines[i - 1])
            if func_match:
                func_name = func_match.group(1)

        # Find the comment block above the function
        # Walk backwards from the function declaration line
        func_line = i - 1 if i >= 1 else i
        comment_end = -1
        comment_start = -1
        j = func_line
        while j >= 0:
            stripped = lines[j].strip()
            if stripped == '*/':
                comment_end = j
                break
            elif stripped.endswith('*/'):
                comment_end = j
                break
            elif stripped.startswith('unsigned long') or stripped.startswith('return'):
                j -= 1
                continue
            elif stripped == '':
                j -= 1
                continue
            else:
                break
            j -= 1

        if comment_end >= 0:
            j = comment_end - 1
            while j >= 0:
                stripped = lines[j].strip()
                if stripped.startswith('/**') or stripped.startswith('/*'):
                    comment_start = j
                    break
                j -= 1

        comment_block = ""
        if comment_start >= 0 and comment_end >= 0:
            comment_block = "".join(lines[comment_start:comment_end + 1])

        entry = HapEntry(
            cpp_func=func_name,
            line_num=return_line_num,
            current_offset=offset,
            entry_type="unknown",
            comment_block=comment_block,
            return_line=line,
        )

        # Parse tags from comment
        for cline in comment_block.split('\n'):
            cline_stripped = cline.strip().lstrip('*').strip()

            # @class
            if '@class' in cline_stripped:
                class_content = cline_stripped.split('@class', 1)[1].strip()
                entry.class_line = class_content
                entry.class_name = extract_class_name(class_content)

            # @field
            elif '@field' in cline_stripped:
                field_content = cline_stripped.split('@field', 1)[1].strip()
                entry.field_line = field_content
                entry.entry_type = "field"
                # Parse field: "private readonly int individualAttack_; // 0x70"
                fm = re.match(
                    r'(?:(public|private|protected|internal)\s+)?'
                    r'(?:(?:static|readonly|const|volatile)\s+)*'
                    r'(.+?)\s+(\w+)\s*;',
                    field_content
                )
                if fm:
                    entry.access = fm.group(1) or ""
                    entry.field_type = fm.group(2).strip()
                    entry.field_name = fm.group(3)

            # @method
            elif '@method' in cline_stripped:
                method_content = cline_stripped.split('@method', 1)[1].strip()
                entry.method_sig = method_content
                entry.entry_type = "method"
                access, modifiers, ret_type, name, ptypes = parse_method_signature(method_content)
                entry.access = access
                entry.modifiers = modifiers
                entry.return_type = ret_type
                entry.method_name = name
                entry.param_types = ptypes

            # @slot
            elif '@slot' in cline_stripped:
                slot_content = cline_stripped.split('@slot', 1)[1].strip()
                try:
                    entry.slot = int(slot_content)
                except ValueError:
                    pass

            # @position
            elif '@position' in cline_stripped:
                pos_content = cline_stripped.split('@position', 1)[1].strip()
                try:
                    entry.position = int(pos_content)
                except ValueError:
                    pass

            # @generic
            elif '@generic' in cline_stripped:
                entry.generic_key = cline_stripped.split('@generic', 1)[1].strip()

            # @comment
            elif '@comment' in cline_stripped:
                entry.comment_hint = cline_stripped.split('@comment', 1)[1].strip()

            # @deprecated
            elif '@deprecated' in cline_stripped:
                entry.entry_type = "deprecated"

        # Default to unknown if no type set
        if entry.entry_type == "unknown" and entry.class_name:
            # Has a class but no @field or @method -- skip
            pass

        entries.append(entry)
        i += 1

    return entries


# ─── dump.cs Parser ──────────────────────────────────────────────────────────

def parse_dump_cs(filepath: str) -> Dict[str, DumpClass]:
    """Parse dump.cs and return a dict of class_name -> DumpClass."""
    classes: Dict[str, DumpClass] = {}
    current_class: Optional[DumpClass] = None
    brace_depth = 0
    pending_rva = None
    pending_slot = None
    pending_rva_line = 0
    in_generic_inst = False
    generic_rva = None
    generic_lines = []

    # For tracking class boundaries
    class_stack = []

    print(f"{C.DIM}Parsing dump.cs...{C.RESET}", end=" ", flush=True)

    with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
        for line_num, line in enumerate(f, 1):
            stripped = line.strip()

            # Track brace depth for class boundaries
            brace_depth += stripped.count('{') - stripped.count('}')

            # Detect class definitions (at top-level or nested)
            class_match = re.match(
                r'^(?:public|private|protected|internal)?\s*'
                r'(?:static\s+|sealed\s+|abstract\s+)*'
                r'class\s+(\S+)',
                stripped
            )
            if class_match and not stripped.startswith('//'):
                cname = class_match.group(1).rstrip(':')
                cname = re.sub(r'<.*$', '', cname)  # Remove generics
                dc = DumpClass(name=cname, full_line=stripped)
                # Store by name (last definition wins for same name)
                if cname not in classes:
                    classes[cname] = dc
                current_class = classes[cname]
                class_stack.append((brace_depth, current_class))
                pending_rva = None
                in_generic_inst = False
                continue

            # Track when we leave a class
            if class_stack and brace_depth < class_stack[-1][0]:
                class_stack.pop()
                current_class = class_stack[-1][1] if class_stack else None

            if not current_class:
                pending_rva = None
                continue

            # Parse field declarations: "private Type name; // 0xXX"
            field_match = re.match(
                r'^(?:(?:public|private|protected|internal)\s+)?'
                r'(?:(?:static|readonly|const|volatile)\s+)*'
                r'(.+?)\s+(\w+)\s*;\s*//\s*(0x[0-9a-fA-F]+)',
                stripped
            )
            if field_match and not stripped.startswith('//') and not stripped.startswith('['):
                ftype = field_match.group(1).strip()
                fname = field_match.group(2)
                foffset = field_match.group(3)
                df = DumpField(name=fname, field_type=ftype, offset=foffset,
                               line_num=line_num, raw_line=stripped)
                current_class.fields.append(df)

            # Parse RVA comment: "// RVA: 0xXXX Offset: 0xXXX VA: 0xXXX [Slot: N]"
            rva_match = re.match(
                r'^//\s*RVA:\s*(0x[0-9a-fA-F]+|-1)\s+Offset:\s*\S+\s+VA:\s*\S+\s*(?:Slot:\s*(\d+))?',
                stripped
            )
            if rva_match:
                pending_rva = rva_match.group(1)
                pending_slot = int(rva_match.group(2)) if rva_match.group(2) else None
                pending_rva_line = line_num
                continue

            # Parse GenericInstMethod blocks
            if stripped == '/* GenericInstMethod :':
                in_generic_inst = True
                generic_rva = None
                generic_lines = []
                continue

            if in_generic_inst:
                if stripped == '*/':
                    in_generic_inst = False
                    generic_rva = None
                    continue
                # Look for "|-RVA: 0xXXX"
                gi_rva = re.match(r'^\|?-?RVA:\s*(0x[0-9a-fA-F]+)', stripped)
                if gi_rva:
                    generic_rva = gi_rva.group(1)
                    continue
                # Look for "|-ClassName.MethodName<T1, T2>"
                gi_name = re.match(r'^\|?-?(.+\..+)', stripped)
                if gi_name and generic_rva:
                    key = gi_name.group(1).strip()
                    if key not in current_class.generic_inst:
                        current_class.generic_inst[key] = []
                    current_class.generic_inst[key].append(generic_rva)
                    generic_rva = None
                continue

            # Parse method declaration (comes after an RVA comment)
            if pending_rva is not None and not stripped.startswith('//') and not stripped.startswith('[') and not stripped.startswith('/*'):
                # Check if this looks like a method
                method_match = re.match(
                    r'^(?:(public|private|protected|internal)\s+)?'
                    r'((?:(?:static|virtual|override|abstract|sealed|new|extern|readonly)\s+)*)'
                    r'(.+?)\s+(\w+)\s*'
                    r'(?:<[^>]*>)?\s*'
                    r'\(([^)]*)\)\s*\{?\s*\}?\s*;?\s*$',
                    stripped
                )
                if method_match:
                    access = method_match.group(1) or ""
                    mods_str = method_match.group(2).strip()
                    mods = mods_str.split() if mods_str else []
                    ret_type = method_match.group(3).strip()
                    mname = method_match.group(4)
                    params_str = method_match.group(5).strip()

                    # Extract generic part from method name
                    full_name_match = re.search(r'(\w+)\s*(<[^>]*>)?\s*\(', stripped)
                    if full_name_match:
                        mname = full_name_match.group(1)

                    ptypes = []
                    if params_str:
                        for p in split_params(params_str):
                            p = p.strip()
                            p = re.sub(r'\s*=\s*[^,]+$', '', p).strip()
                            parts = p.rsplit(None, 1)
                            if len(parts) >= 2:
                                ptypes.append(parts[0].strip())
                            elif len(parts) == 1:
                                ptypes.append(parts[0].strip())

                    dm = DumpMethod(
                        name=mname,
                        rva=pending_rva,
                        return_type=ret_type,
                        param_types=ptypes,
                        access=access,
                        modifiers=mods,
                        slot=pending_slot,
                        line_num=pending_rva_line,
                        position_in_class=len(current_class.methods),
                        raw_line=stripped,
                    )
                    current_class.methods.append(dm)
                
                pending_rva = None
                pending_slot = None

    print(f"{C.GREEN}done{C.RESET} ({len(classes)} classes)")
    return classes


# ─── Matching Engine ─────────────────────────────────────────────────────────

@dataclass
class MatchResult:
    status: str          # "updated", "unchanged", "skipped", "warn", "failed"
    new_offset: str = ""
    new_method_name: str = ""  # for updating @method tag
    new_field_line: str = ""   # for updating @field tag
    message: str = ""
    matched_via: str = ""


def normalize_type(t: str) -> str:
    """Normalize a C# type for comparison."""
    t = t.strip()
    # Normalize common aliases
    aliases = {
        'Void': 'void', 'Boolean': 'bool', 'Int32': 'int',
        'Int64': 'long', 'UInt64': 'ulong', 'Single': 'float',
        'Double': 'double', 'String': 'string',
    }
    if t in aliases:
        return aliases[t]
    return t


def simple_type_name(t: str) -> str:
    """Get the simple (unqualified) type name, e.g. 'Foo.Bar.Baz' -> 'Baz'."""
    t = normalize_type(t)
    # Handle nested types like CombatProto.Types.CombatState -> CombatState
    parts = t.split('.')
    return parts[-1] if parts else t


def types_match(a: str, b: str) -> bool:
    """Check if two types are equivalent, considering qualified vs simple names."""
    na, nb = normalize_type(a), normalize_type(b)
    if na == nb:
        return True
    # Also match if simple names are the same (handles Foo.Bar.X vs X)
    return simple_type_name(na) == simple_type_name(nb)


def param_types_match(a: List[str], b: List[str]) -> bool:
    """Check if two param type lists match."""
    if len(a) != len(b):
        return False
    return all(types_match(x, y) for x, y in zip(a, b))


def sig_fingerprint(access: str, return_type: str, param_types: List[str]) -> str:
    """Create a comparable signature fingerprint using simple type names."""
    rt = simple_type_name(return_type)
    pts = ",".join(simple_type_name(p) for p in param_types)
    return f"{access} {rt}({pts})"


def match_field(entry: HapEntry, classes: Dict[str, DumpClass]) -> MatchResult:
    """Match a @field entry against dump.cs."""
    if not entry.class_name or not entry.field_name:
        return MatchResult("failed", message="Missing class name or field name")

    dc = classes.get(entry.class_name)
    if not dc:
        return MatchResult("failed", message=f"Class '{entry.class_name}' not found in dump.cs")

    # Try exact field name match
    for df in dc.fields:
        if df.name == entry.field_name:
            if df.offset.lower() == entry.current_offset.lower():
                return MatchResult("unchanged", new_offset=df.offset,
                                   message=f"{entry.class_name}.{df.name}")
            return MatchResult("updated", new_offset=df.offset,
                               message=f"{entry.class_name}.{df.name}",
                               matched_via="field name")

    # Fall back to type-based matching for obfuscated field names
    type_matches = [df for df in dc.fields if df.field_type == entry.field_type]
    if len(type_matches) == 1:
        df = type_matches[0]
        new_field_line = entry.field_line.replace(entry.field_name, df.name)
        if df.offset.lower() == entry.current_offset.lower():
            return MatchResult("unchanged", new_offset=df.offset,
                               new_field_line=new_field_line,
                               message=f"{entry.class_name}.{df.name} (was {entry.field_name})")
        return MatchResult("updated", new_offset=df.offset,
                           new_field_line=new_field_line,
                           message=f"{entry.class_name}.{df.name} (was {entry.field_name})",
                           matched_via="field type")

    return MatchResult("failed",
                       message=f"Field '{entry.field_name}' not found in {entry.class_name} "
                               f"({len(type_matches)} type matches)")


def _lookup_generic_inst(key: str, entry: HapEntry, classes: Dict[str, DumpClass],
                         position: Optional[int]) -> Optional[MatchResult]:
    """Look up a GenericInstMethod key across all classes, respecting @position for duplicates."""
    for cname, c in classes.items():
        rva_list = c.generic_inst.get(key)
        if rva_list:
            if len(rva_list) == 1:
                rva = rva_list[0]
            elif position is not None and 1 <= position <= len(rva_list):
                rva = rva_list[position - 1]  # 1-indexed
            else:
                # Multiple matches, no @position
                rvas = ", ".join(rva_list)
                return MatchResult("failed",
                                   message=f"Ambiguous generic: {len(rva_list)} entries for "
                                           f"'{key}' [{rvas}]. Add @position to disambiguate.")
            if rva.lower() == entry.current_offset.lower():
                return MatchResult("unchanged", new_offset=rva,
                                   message=f"GenericInst: {key}")
            status = "updated" if len(rva_list) == 1 else "warn"
            return MatchResult(status, new_offset=rva,
                               message=f"GenericInst: {key}" +
                                       (f" (position {position}/{len(rva_list)})" if position else ""),
                               matched_via="@generic")
    return None


def match_method(entry: HapEntry, classes: Dict[str, DumpClass]) -> MatchResult:
    """Match a @method entry against dump.cs."""
    if not entry.class_name:
        return MatchResult("failed", message="Missing class name")

    dc = classes.get(entry.class_name)
    if not dc:
        return MatchResult("failed", message=f"Class '{entry.class_name}' not found in dump.cs")

    # Filter out abstract methods (RVA: -1) unless they have generic inst
    concrete_methods = [m for m in dc.methods if m.rva != "-1"]

    # ── Strategy 1: @generic match ──
    if entry.generic_key:
        result = _lookup_generic_inst(entry.generic_key, entry, classes, entry.position)
        if result:
            return result
        return MatchResult("failed", message=f"Generic key '{entry.generic_key}' not found")

    # ── Strategy 1b: @comment with GenericInstMethod hint ──
    if entry.comment_hint and entry.comment_hint.startswith('|-'):
        key = entry.comment_hint[2:].strip()
        result = _lookup_generic_inst(key, entry, classes, None)
        if result:
            return result
        return MatchResult("failed", message=f"Generic key '{key}' not found")

    # ── Strategy 2: @slot match ──
    if entry.slot is not None:
        slot_matches = [m for m in concrete_methods if m.slot == entry.slot]
        if len(slot_matches) == 1:
            dm = slot_matches[0]
            new_name = dm.name
            if dm.rva.lower() == entry.current_offset.lower():
                return MatchResult("unchanged", new_offset=dm.rva,
                                   new_method_name=new_name,
                                   message=f"{entry.class_name}.{dm.name} (slot {entry.slot})")
            return MatchResult("updated", new_offset=dm.rva,
                               new_method_name=new_name,
                               message=f"{entry.class_name}.{dm.name} (slot {entry.slot})",
                               matched_via="@slot")
        elif len(slot_matches) == 0:
            pass  # Fall through to other strategies
        else:
            return MatchResult("failed",
                               message=f"Multiple methods with slot {entry.slot} in {entry.class_name}")

    # ── Strategy 3: Name-based match ──
    if entry.method_name:
        name_matches = [m for m in concrete_methods if m.name == entry.method_name]
        if len(name_matches) == 1:
            dm = name_matches[0]
            if dm.rva.lower() == entry.current_offset.lower():
                return MatchResult("unchanged", new_offset=dm.rva,
                                   message=f"{entry.class_name}.{dm.name}")
            return MatchResult("updated", new_offset=dm.rva,
                               message=f"{entry.class_name}.{dm.name}",
                               matched_via="name")
        elif len(name_matches) > 1:
            # Disambiguate by param types
            if entry.param_types:
                refined = [m for m in name_matches if param_types_match(entry.param_types, m.param_types)]
                if len(refined) == 1:
                    dm = refined[0]
                    if dm.rva.lower() == entry.current_offset.lower():
                        return MatchResult("unchanged", new_offset=dm.rva,
                                           message=f"{entry.class_name}.{dm.name}")
                    return MatchResult("updated", new_offset=dm.rva,
                                       message=f"{entry.class_name}.{dm.name}",
                                       matched_via="name+params")

    # ── Strategy 4: Signature-based match ──
    entry_fp = sig_fingerprint(entry.access, entry.return_type, entry.param_types)
    sig_matches = [
        m for m in concrete_methods
        if sig_fingerprint(m.access, m.return_type, m.param_types) == entry_fp
    ]

    if len(sig_matches) == 1:
        dm = sig_matches[0]
        new_name = dm.name
        if dm.rva.lower() == entry.current_offset.lower():
            return MatchResult("unchanged", new_offset=dm.rva,
                               new_method_name=new_name,
                               message=f"{entry.class_name}.{dm.name}")
        return MatchResult("updated", new_offset=dm.rva,
                           new_method_name=new_name,
                           message=f"{entry.class_name}.{dm.name} (was {entry.method_name})",
                           matched_via="signature")

    # ── Strategy 5: @position match ──
    if entry.position is not None and len(sig_matches) >= entry.position:
        # Sort by position in class (order of appearance)
        sig_matches_sorted = sorted(sig_matches, key=lambda m: m.position_in_class)
        dm = sig_matches_sorted[entry.position - 1]  # 1-indexed
        new_name = dm.name
        if dm.rva.lower() == entry.current_offset.lower():
            return MatchResult("unchanged", new_offset=dm.rva,
                               new_method_name=new_name,
                               message=f"{entry.class_name}.{dm.name} (position {entry.position}/{len(sig_matches)})")
        return MatchResult("warn", new_offset=dm.rva,
                           new_method_name=new_name,
                           message=f"{entry.class_name}.{dm.name} position {entry.position}/{len(sig_matches)} (was {entry.method_name})",
                           matched_via="@position")

    if len(sig_matches) > 1:
        names = ", ".join(f"{m.name}@{m.rva}" for m in sig_matches)
        return MatchResult("failed",
                           message=f"Ambiguous: {len(sig_matches)} matches for sig '{entry_fp}' in {entry.class_name}: [{names}]")

    return MatchResult("failed",
                       message=f"No match for {entry.class_name}.{entry.method_name} "
                               f"(sig: {entry_fp})")


def match_entry(entry: HapEntry, classes: Dict[str, DumpClass]) -> MatchResult:
    """Match a single HAP entry against dump.cs."""
    if entry.entry_type == "deprecated":
        return MatchResult("skipped", message="@deprecated")

    if entry.entry_type == "field":
        return match_field(entry, classes)

    if entry.entry_type == "method":
        return match_method(entry, classes)

    return MatchResult("failed", message="Unknown entry type (missing @field or @method tag)")


# ─── File Updater ────────────────────────────────────────────────────────────

def apply_updates(filepath: str, entries: List[HapEntry], results: List[MatchResult]):
    """Apply offset and name updates to HAP64.cpp."""
    with open(filepath, 'r', encoding='utf-8', errors='replace') as f:
        content = f.read()

    for entry, result in zip(entries, results):
        if result.status not in ("updated", "warn", "unchanged"):
            continue

        # Update the return value offset
        if result.status in ("updated", "warn"):
            old_return = f"return {entry.current_offset};"
            new_return = f"return {result.new_offset};"
            # Be specific: only replace in the context of this function
            content = content.replace(old_return, new_return, 1)

        # Update the @method obfuscated name if it changed
        if result.new_method_name and result.new_method_name != entry.method_name and entry.method_sig:
            old_sig = entry.method_sig
            # Replace the old method name with the new one in the signature
            new_sig = re.sub(
                r'\b' + re.escape(entry.method_name) + r'\b',
                result.new_method_name,
                old_sig,
                count=1
            )
            if new_sig != old_sig:
                content = content.replace(old_sig, new_sig, 1)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)


# ─── Report ──────────────────────────────────────────────────────────────────

def print_report(entries: List[HapEntry], results: List[MatchResult]):
    """Print a summary report."""
    updated = sum(1 for r in results if r.status == "updated")
    unchanged = sum(1 for r in results if r.status == "unchanged")
    warned = sum(1 for r in results if r.status == "warn")
    skipped = sum(1 for r in results if r.status == "skipped")
    failed = sum(1 for r in results if r.status == "failed")

    print(f"\n{'='*70}")
    print(f"  HAP64 Offset Update Report")
    print(f"{'='*70}\n")

    for entry, result in zip(entries, results):
        func = entry.cpp_func or "???"
        if result.status == "updated":
            print(f"  {C.GREEN}UPDATED{C.RESET}   {func:20s}  "
                  f"{entry.current_offset} -> {C.BOLD}{result.new_offset}{C.RESET}  "
                  f"{C.DIM}({result.matched_via}: {result.message}){C.RESET}")
        elif result.status == "warn":
            print(f"  {C.YELLOW}WARN{C.RESET}      {func:20s}  "
                  f"{entry.current_offset} -> {C.BOLD}{result.new_offset}{C.RESET}  "
                  f"{C.YELLOW}({result.matched_via}: {result.message}){C.RESET}")
        elif result.status == "unchanged":
            print(f"  {C.DIM}UNCHANGED {func:20s}  {entry.current_offset}  ({result.message}){C.RESET}")
        elif result.status == "skipped":
            print(f"  {C.DIM}SKIPPED   {func:20s}  ({result.message}){C.RESET}")
        elif result.status == "failed":
            print(f"  {C.RED}FAILED{C.RESET}    {func:20s}  "
                  f"{C.RED}{result.message}{C.RESET}")

    print(f"\n{'─'*70}")
    print(f"  {C.GREEN}{updated} updated{C.RESET}, "
          f"{C.YELLOW}{warned} warnings{C.RESET}, "
          f"{unchanged} unchanged, "
          f"{skipped} skipped, "
          f"{C.RED}{failed} failed{C.RESET}")
    print(f"{'─'*70}\n")

    return failed


# ─── Main ────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Update HAP64.cpp offsets from dump.cs")
    script_dir = os.path.dirname(os.path.abspath(__file__))
    parser.add_argument("--hap", default=os.path.join(script_dir, "HAP64.cpp"),
                        help="Path to HAP64.cpp")
    parser.add_argument("--dump", default=os.path.join(script_dir, "dump.cs"),
                        help="Path to dump.cs")
    parser.add_argument("--dry-run", action="store_true",
                        help="Preview changes without writing")
    parser.add_argument("--no-color", action="store_true",
                        help="Disable colored output")
    args = parser.parse_args()

    if args.no_color or not sys.stdout.isatty():
        C.disable()

    # Validate files exist
    if not os.path.isfile(args.hap):
        print(f"{C.RED}Error: HAP64.cpp not found at {args.hap}{C.RESET}")
        sys.exit(1)
    if not os.path.isfile(args.dump):
        print(f"{C.RED}Error: dump.cs not found at {args.dump}{C.RESET}")
        sys.exit(1)

    # Parse
    print(f"{C.BOLD}HAP64 Offset Updater{C.RESET}\n")
    entries = parse_hap64(args.hap)
    print(f"  Parsed {len(entries)} entries from HAP64.cpp")

    classes = parse_dump_cs(args.dump)

    # Match
    print(f"\n{C.DIM}Matching...{C.RESET}")
    results = [match_entry(e, classes) for e in entries]

    # Report
    failed = print_report(entries, results)

    # Apply updates
    if not args.dry_run:
        has_changes = any(r.status in ("updated", "warn") for r in results)
        if has_changes:
            apply_updates(args.hap, entries, results)
            print(f"  {C.GREEN}Changes written to {args.hap}{C.RESET}\n")
        else:
            print(f"  {C.DIM}No changes to write.{C.RESET}\n")
    else:
        print(f"  {C.YELLOW}Dry run -- no changes written.{C.RESET}\n")

    sys.exit(1 if failed > 0 else 0)


if __name__ == "__main__":
    main()
