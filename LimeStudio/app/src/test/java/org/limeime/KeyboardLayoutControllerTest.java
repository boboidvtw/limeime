package org.limeime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.limeime.keyboard.PhoneKeyboardModePolicy;

public class KeyboardLayoutControllerTest {

    @Test
    public void cycleModeSequencesCorrectly() {
        int mode = PhoneKeyboardModePolicy.PORTRAIT_STANDARD;

        // Standard -> Right
        mode = getNextMode(mode);
        assertEquals(PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_RIGHT, mode);

        // Right -> Left
        mode = getNextMode(mode);
        assertEquals(PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_LEFT, mode);

        // Left -> Standard
        mode = getNextMode(mode);
        assertEquals(PhoneKeyboardModePolicy.PORTRAIT_STANDARD, mode);
    }

    private int getNextMode(int current) {
        switch (current) {
            case PhoneKeyboardModePolicy.PORTRAIT_STANDARD:
                return PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_RIGHT;
            case PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_RIGHT:
                return PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_LEFT;
            case PhoneKeyboardModePolicy.PORTRAIT_ONE_HAND_LEFT:
            default:
                return PhoneKeyboardModePolicy.PORTRAIT_STANDARD;
        }
    }
}
