package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Independent regression witness for the public behavior reported in jhy/jsoup#2638. */
class ReportedShallowCloneCacheTest {
    @Test
    void elementShallowCloneReportsItsOwnAppendedChildren() {
        assertOwnChildren(Jsoup.parse("<p>One</p><p>Two</p>").body());
    }

    @Test
    void documentShallowCloneReportsItsOwnAppendedChildren() {
        assertOwnChildren(Jsoup.parse("<p>One</p><p>Two</p>"));
    }

    private static void assertOwnChildren(Element original) {
        original.children();
        // Keep the existing weakly cached list alive; no internal state is changed.
        List<Element> cachedOriginalChildren = original.childElementsList();
        Element copy = original.shallowClone();
        for (Node child : original.childNodes()) {
            copy.appendChild(child.shallowClone());
        }

        Elements reported = copy.children();
        assertEquals(cachedOriginalChildren.size(), reported.size());
        for (int i = 0; i < reported.size(); i++) {
            Node actualChild = copy.childNode(i);
            assertSame(copy, actualChild.parent(), "The appended node must belong to the clone");
            assertSame(actualChild, reported.get(i),
                "children() must return the clone's actual appended element");
            assertSame(copy, reported.get(i).parent(),
                "A reported child must have the clone as its parent");
            assertNotSame(cachedOriginalChildren.get(i), reported.get(i),
                "A shallow clone must not report an original child as its own");
            assertSame(original, cachedOriginalChildren.get(i).parent());
        }
    }
}
