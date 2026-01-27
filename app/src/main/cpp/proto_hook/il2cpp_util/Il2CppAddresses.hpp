//
//
//

#ifndef POGODROID_IL2CPPADDRESSES_HPP
#define POGODROID_IL2CPPADDRESSES_HPP
#include "frida-gumjs.h"

namespace Il2CppUtil {

    class Il2CppAddresses {
    public:
        Il2CppAddresses(const Il2CppAddresses&) = delete;
        Il2CppAddresses& operator=(const Il2CppAddresses &) = delete;
        Il2CppAddresses(Il2CppAddresses &&) = delete;
        Il2CppAddresses & operator=(Il2CppAddresses &&) = delete;

        static auto& instance(){
            static Il2CppAddresses Il2CppAddresses;
            return Il2CppAddresses;
        }
        GumAddress il2cpp_domain_get() {
            if (this->value_il2cpp_domain_get == 0) {
                this->value_il2cpp_domain_get = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_domain_get");
            }
            return this->value_il2cpp_domain_get;
        }

        GumAddress il2cpp_domain_get_assemblies() {
            if (this->value_il2cpp_domain_get_assemblies == 0) {
                this->value_il2cpp_domain_get_assemblies = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_domain_get_assemblies");
            }
            return this->value_il2cpp_domain_get_assemblies;
        }

        GumAddress il2cpp_assembly_get_image() {
            if (this->value_il2cpp_assembly_get_image == 0) {
                this->value_il2cpp_assembly_get_image = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_assembly_get_image");
            }
            return this->value_il2cpp_assembly_get_image;
        }

        GumAddress il2cpp_image_get_class_count() {
            if (this->value_il2cpp_image_get_class_count == 0) {
                this->value_il2cpp_image_get_class_count = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_image_get_class_count");
            }
            return this->value_il2cpp_image_get_class_count;
        }

        GumAddress il2cpp_image_get_class() {
            if (this->value_il2cpp_image_get_class == 0) {
                this->value_il2cpp_image_get_class = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_image_get_class");
            }
            return this->value_il2cpp_image_get_class;
        }

        GumAddress il2cpp_class_get_methods() {
            if (this->value_il2cpp_class_get_methods == 0) {
                this->value_il2cpp_class_get_methods = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_class_get_methods");
            }
            return this->value_il2cpp_class_get_methods;
        }

        GumAddress il2cpp_method_get_name() {
            if (this->value_il2cpp_method_get_name == 0) {
                this->value_il2cpp_method_get_name = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_method_get_name");
            }
            return this->value_il2cpp_method_get_name;
        }

        GumAddress il2cpp_method_get_return_type() {
            if (this->value_il2cpp_method_get_return_type == 0) {
                this->value_il2cpp_method_get_return_type = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_method_get_return_type");
            }
            return this->value_il2cpp_method_get_return_type;
        }

        GumAddress il2cpp_type_get_name() {
            if (this->value_il2cpp_type_get_name == 0) {
                this->value_il2cpp_type_get_name = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_type_get_name");
            }
            return this->value_il2cpp_type_get_name;
        }

        GumAddress il2cpp_free() {
            if (this->value_il2cpp_free == 0) {
                this->value_il2cpp_free = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_free");
            }
            return this->value_il2cpp_free;
        }

        GumAddress il2cpp_method_get_param_count() {
            if (this->value_il2cpp_method_get_param_count == 0) {
                this->value_il2cpp_method_get_param_count = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_method_get_param_count");
            }
            return this->value_il2cpp_method_get_param_count;
        }

        GumAddress il2cpp_object_new() {
            if (this->value_il2cpp_object_new == 0) {
                this->value_il2cpp_object_new = gum_module_find_export_by_name("libil2cpp.so", "il2cpp_object_new");
            }
            return this->value_il2cpp_object_new;
        }

    private:
        Il2CppAddresses () = default; /* verhindert, dass ein Objekt von außerhalb von N erzeugt wird. */
        GumAddress value_il2cpp_domain_get = 0;
        GumAddress value_il2cpp_domain_get_assemblies = 0;
        GumAddress value_il2cpp_assembly_get_image = 0;
        GumAddress value_il2cpp_image_get_class_count = 0;
        GumAddress value_il2cpp_image_get_class = 0;
        GumAddress value_il2cpp_class_get_methods = 0;
        GumAddress value_il2cpp_method_get_name = 0;
        GumAddress value_il2cpp_method_get_return_type = 0;
        GumAddress value_il2cpp_type_get_name = 0;
        GumAddress value_il2cpp_free = 0;
        GumAddress value_il2cpp_method_get_param_count = 0;
        GumAddress value_il2cpp_object_new = 0;


    };

} // Il2CppUtil

#endif //POGODROID_IL2CPPADDRESSES_HPP
