//
//
//

#ifndef POGODROID_PROFILEREPORTIMPL_H
#define POGODROID_PROFILEREPORTIMPL_H

#include "ObjectWrapper.hpp"
#include "structs.h"
#include "../frida-gumjs.h"

namespace Gum {
    class ProfileReportImpl : public ObjectWrapper<ProfileReportImpl, ProfileReport, GumProfileReport> {
    public:
        ProfileReportImpl (GumProfileReport * handle)
        {
            assign_handle (handle);
        }

        virtual String * emit_xml ()
        {
            return new StringImpl (gum_profile_report_emit_xml (handle));
        }
    };
    };
}

#endif //POGODROID_PROFILEREPORTIMPL_H
