package views.util;

import play.api.mvc.Call;

// Our requirements for an enumerated type are:
//
//     1. We can associate arbitrary data with values (specifically, description
//        and call).
//
//     2. It's possible to enumerate values without manually maintaining a list
//        of them.
//
// Nothing I've seen in the Scala space fulfills those two requirements like a
// Java enum, so that's what we use.
public enum NavbarItem {

    DESIGN ("Design", controllers.routes.Application.designLabels()),
    ABOUT ("About", controllers.routes.Application.showAbout());

    private String description;
    private Call call;
    // From a Call, Play can generate a URL. Such a URL would be the more
    // obvious choice for this enum's second field. However, generating that URL
    // on enum initialization might be difficult: most of Call's absoluteURL()
    // methods require a Request/RequestHeader, which is not available at enum
    // initialization.

    private NavbarItem(String description, Call call) {
        this.description = description;
        this.call = call;
    }

    public String getDescription() {
        return description;
    }

    public Call getCall() {
        return call;
    }
}
