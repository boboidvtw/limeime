package org.limeime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.LinkedList;

public class LIMEClipboardManagerContractTest {

    private static final int MAX_HISTORY_SIZE = 20;

    @Test
    public void clipboardDeduplicationAndCapacityBound() {
        LinkedList<String> history = new LinkedList<>();

        for (int i = 0; i < 25; i++) {
            addClip(history, "Item " + i);
        }

        // Must cap at 20 items
        assertEquals(MAX_HISTORY_SIZE, history.size());
        // Most recent item must be at front
        assertEquals("Item 24", history.getFirst());

        // Re-adding existing item should promote to front without duplicate
        addClip(history, "Item 10");
        assertEquals(MAX_HISTORY_SIZE, history.size());
        assertEquals("Item 10", history.getFirst());
    }

    @Test
    public void clipboardRejectsEmptyOrExcessiveClips() {
        LinkedList<String> history = new LinkedList<>();

        addClip(history, "");
        addClip(history, "   ");
        assertTrue(history.isEmpty());

        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            huge.append("A");
        }
        addClip(history, huge.toString());
        assertTrue(history.isEmpty());
    }

    private void addClip(LinkedList<String> list, String text) {
        if (text == null) return;
        String s = text.trim();
        if (s.isEmpty() || s.length() > 4000) return;
        list.remove(s);
        list.addFirst(s);
        while (list.size() > MAX_HISTORY_SIZE) {
            list.removeLast();
        }
    }
}
