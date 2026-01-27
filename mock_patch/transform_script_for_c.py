
with open("script_to_use.js", "r") as script:
    with open("transformed.js", "w") as transformed:
        for line in script.readlines():
            new_line = line.strip().replace('"', '\\"')
            transformed.write(f"\"{new_line}\\n\"\n")
