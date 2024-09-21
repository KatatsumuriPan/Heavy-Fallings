/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package kpan.heavy_fallings.config.core.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kpan.heavy_fallings.config.core.properties.ConfigPropertyList;
import kpan.heavy_fallings.config.core.properties.PropertyValueType.TypeEnum;
import kpan.heavy_fallings.util.IBlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;
import static net.minecraftforge.fml.client.config.GuiUtils.INVALID;
import static net.minecraftforge.fml.client.config.GuiUtils.VALID;
import net.minecraftforge.fml.client.config.HoverChecker;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

/**
 * This class implements the scrolling list functionality of the GuiEditList screen. It also provides all the default controls
 * for editing array-type properties.
 */
public class ModGuiEditArrayEntries extends GuiListExtended {
    protected ModGuiEditArray owningGui;
    public ConfigPropertyList propertyList;
    public List<IArrayEntry> listEntries;
    public boolean isDefault;
    public boolean isChanged;
    public boolean canAddMoreEntries;
    public final int controlWidth;
    public final Object[] beforeValues;
    public Object[] currentValues;

    public ModGuiEditArrayEntries(ModGuiEditArray parent, Minecraft mc, ConfigPropertyList propertyList, Object[] beforeValues, Object[] currentValues) {
        super(mc, parent.width, parent.height, parent.titleLine2 != null ? (parent.titleLine3 != null ? 43 : 33) : 23, parent.height - 32, 20);
        this.owningGui = parent;
        this.propertyList = propertyList;
        this.beforeValues = beforeValues;
        this.currentValues = currentValues;
        this.setShowSelectionBox(false);
        this.isChanged = !Arrays.deepEquals(beforeValues, currentValues);
        this.isDefault = Arrays.deepEquals(currentValues, propertyList.getDefaultValues());
        this.canAddMoreEntries = !propertyList.isLengthFixed() || currentValues.length < propertyList.getMaxListLength();

        listEntries = new ArrayList<>();

        controlWidth = (parent.width / 2) - (propertyList.isLengthFixed() ? 0 : 48);

        for (Object value : currentValues) {
            listEntries.add(propertyList.getElementType().createEntry(this, propertyList, value));
        }

        if (!propertyList.isLengthFixed())
            listEntries.add(new BaseEntry(this, propertyList));

    }

    @Override
    protected int getScrollBarX() {
        return width - (width / 4);
    }

    /**
     * Gets the width of the list
     */
    @Override
    public int getListWidth() {
        return owningGui.width;
    }

    /**
     * Gets the IGuiListEntry object for the given index
     */
    @Override
    public @NotNull IArrayEntry getListEntry(int index) {
        return listEntries.get(index);
    }

    @Override
    protected int getSize() {
        return listEntries.size();
    }

    public void addNewEntry(int index) {
        listEntries.add(index, propertyList.getElementType().createNewEntry(this, propertyList));
        this.canAddMoreEntries = !propertyList.isLengthFixed() || this.listEntries.size() - 1 < propertyList.getMaxListLength();
        keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_END);
    }

    public void removeEntry(int index) {
        this.listEntries.remove(index);
        this.canAddMoreEntries = !propertyList.isLengthFixed() || this.listEntries.size() - 1 < propertyList.getMaxListLength();
        keyTyped((char) Keyboard.CHAR_NONE, Keyboard.KEY_END);
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void recalculateState() {
        isDefault = true;
        isChanged = false;

        int listLength = propertyList.isLengthFixed() ? listEntries.size() : listEntries.size() - 1;

        if (listLength != propertyList.getDefaultValues().length) {
            isDefault = false;
        }

        if (listLength != beforeValues.length) {
            isChanged = true;
        }

        if (isDefault)
            for (int i = 0; i < listLength; i++) {
                if (!propertyList.getDefaultValues()[i].equals(listEntries.get(i).getValue()))
                    isDefault = false;
            }

        if (!isChanged)
            for (int i = 0; i < listLength; i++) {
                if (!beforeValues[i].equals(listEntries.get(i).getValue()))
                    isChanged = true;
            }
    }

    protected void keyTyped(char eventChar, int eventKey) {
        for (IArrayEntry entry : this.listEntries) {
            entry.keyTyped(eventChar, eventKey);
        }

        recalculateState();
    }

    protected void updateScreen() {
        for (IArrayEntry entry : this.listEntries) {
            entry.updateCursorCounter();
        }
    }

    protected void mouseClickedPassThru(int x, int y, int mouseEvent) {
        for (IArrayEntry entry : this.listEntries) {
            entry.mouseClicked(x, y, mouseEvent);
        }
    }

    protected boolean isListSavable() {
        for (IArrayEntry entry : this.listEntries) {
            if (!entry.isValueSavable())
                return false;
        }

        return true;
    }

    protected void saveListChanges() {
        int listLength = propertyList.isLengthFixed() ? listEntries.size() : listEntries.size() - 1;
        Object[] ao = new Object[listLength];
        for (int i = 0; i < ao.length; i++) {
            ao[i] = listEntries.get(i).getValue();
        }

        owningGui.parentEntry.setListFromChildScreen(ao);
    }

    protected void drawScreenPost(int mouseX, int mouseY, float f) {
        for (IArrayEntry entry : this.listEntries) {
            entry.drawToolTip(mouseX, mouseY);
        }
    }

    public Minecraft getMC() {
        return this.mc;
    }

    public boolean isEnabled() {
        return owningGui.enabled;
    }

    public void drawToolTip(List<String> stringList, int x, int y) {
        owningGui.drawToolTip(stringList, x, y);
    }

    /**
     * IGuiListEntry Inner Classes
     */

    public static class NumericalEntry extends StringEntry {

        private final boolean isPointAllowed;

        public NumericalEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, Object value, boolean isPointAllowed) {
            super(owningEntryList, configElement, value);
            this.isPointAllowed = isPointAllowed;
            this.hasValidation = true;
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (isValidKeyInput(eventKey)) {
                String validChars = "0123456789";
                String before = this.textFieldValue.getText();
                if (validChars.contains(String.valueOf(eventChar)) ||
                        (!before.startsWith("-") && this.textFieldValue.getCursorPosition() == 0 && eventChar == '-')
                        || (isPointAllowed && !before.contains(".") && eventChar == '.')
                        || eventKey == Keyboard.KEY_BACK || eventKey == Keyboard.KEY_DELETE || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
                        || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END)
                    this.textFieldValue.textboxKeyTyped((owningEntryList.isEnabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

                if (!textFieldValue.getText().trim().isEmpty() && !textFieldValue.getText().trim().equals("-")) {
                    isValidValue = configElement.isValidValue(textFieldValue.getText().trim());
                } else
                    this.isValidValue = false;
            }
        }

    }

    public static class CharEntry extends StringEntry {

        // CharacterとStringの両方を受け付けたいので引数はObject
        public CharEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, Object value) {
            super(owningEntryList, configElement, value);
            isValidValue = configElement.isValidValue(textFieldValue.getText());
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (isValidKeyInput(eventKey)) {
                this.textFieldValue.textboxKeyTyped((owningEntryList.isEnabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);

                if (textFieldValue.getText().length() >= 2)
                    textFieldValue.setText(textFieldValue.getText().substring(1, 2));

                isValidValue = configElement.isValidValue(textFieldValue.getText());
            }
        }

    }

    public static class BlockPredicateEntry extends StringEntry {

        public BlockPredicateEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement) {
            super(owningEntryList, configElement, IBlockPredicate.parse(""));
            textFieldValue.setText("");
        }

        public BlockPredicateEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, Object value) {
            super(owningEntryList, configElement, value);
        }

    }

    public static class StringEntry extends BaseEntry {
        protected final GuiTextField textFieldValue;

        public StringEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, Object value) {
            super(owningEntryList, configElement);
            this.textFieldValue = new GuiTextField(0, owningEntryList.getMC().fontRenderer, owningEntryList.width / 4 + 1, 0, owningEntryList.controlWidth - 3, 16);
            this.textFieldValue.setMaxStringLength(10000);
            this.textFieldValue.setText(configElement.getElementType().toString(configElement.cast(value)));

            isValidValue = configElement.isValidValue(textFieldValue.getText().trim());
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
            if (configElement.isLengthFixed() || slotIndex != owningEntryList.listEntries.size() - 1) {
                this.textFieldValue.setVisible(true);
                this.textFieldValue.y = y + 1;
                this.textFieldValue.drawTextBox();
            } else
                this.textFieldValue.setVisible(false);
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) {
            if (isValidKeyInput(eventKey)) {
                this.textFieldValue.textboxKeyTyped((owningEntryList.isEnabled() ? eventChar : Keyboard.CHAR_NONE), eventKey);
                isValidValue = configElement.isValidValue(textFieldValue.getText().trim());
            }
        }

        @Override
        public void updateCursorCounter() {
            this.textFieldValue.updateCursorCounter();
        }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent) {
            this.textFieldValue.mouseClicked(x, y, mouseEvent);
        }

        @Override
        public Object getValue() {
            return configElement.getElementType().readValue(this.textFieldValue.getText().trim());
        }

        protected boolean isValidKeyInput(int eventKey) {
            return owningEntryList.isEnabled() || eventKey == Keyboard.KEY_LEFT || eventKey == Keyboard.KEY_RIGHT
                    || eventKey == Keyboard.KEY_HOME || eventKey == Keyboard.KEY_END;
        }

    }

    public static class BooleanEntry extends ButtonEntry {
        private boolean value;

        public BooleanEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, boolean value) {
            super(owningEntryList, configElement, I18n.format(configElement.getElementType().toString(configElement.cast(value))));
            this.value = value;
        }

        @Override
        protected String getButtonString() {
            return I18n.format(configElement.getElementType().toString(configElement.cast(value)));
        }

        @Override
        protected void onPressed() {
            value = !value;
        }

        @Override
        public Object getValue() {
            return value;
        }
    }

    public static class EnumEntry extends ButtonEntry {
        private final Enum<?>[] enumValues;
        private Enum<?> value;
        private int currentIndex;

        public EnumEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, Enum<?> value) {
            super(owningEntryList, configElement, configElement.getElementType().toString(configElement.cast(value)));
            this.value = value;
            enumValues = ((TypeEnum<?>) configElement.getElementType()).getEnumClass().getEnumConstants();
            currentIndex = ArrayUtils.indexOf(enumValues, value);
        }


        @Override
        protected String getButtonString() {
            return configElement.getElementType().toString(configElement.cast(value));
        }

        @Override
        protected void onPressed() {
            currentIndex++;
            if (currentIndex >= enumValues.length)
                currentIndex -= enumValues.length;
            value = enumValues[currentIndex];
        }

        @Override
        public Object getValue() {
            return value;
        }

    }

    public static abstract class ButtonEntry extends BaseEntry {
        protected final GuiButtonExt btnValue;

        public ButtonEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement, String initialButtonString) {
            super(owningEntryList, configElement);
            this.btnValue = new GuiButtonExt(0, 0, 0, owningEntryList.controlWidth, 18, I18n.format(initialButtonString));
            this.btnValue.enabled = owningEntryList.isEnabled();
        }

        protected abstract String getButtonString();
        protected abstract void onPressed();

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
            super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
            this.btnValue.x = listWidth / 4;
            this.btnValue.y = y;
            this.btnValue.displayString = getButtonString();
            this.btnValue.drawButton(owningEntryList.getMC(), mouseX, mouseY, partial);
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnValue.mousePressed(owningEntryList.getMC(), x, y)) {
                btnValue.playPressSound(owningEntryList.getMC().getSoundHandler());
                onPressed();
                owningEntryList.recalculateState();
                return true;
            }

            return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnValue.mouseReleased(x, y);
            super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
        }

    }

    public static class BaseEntry implements IArrayEntry {
        protected final ModGuiEditArrayEntries owningEntryList;
        protected final ConfigPropertyList configElement;
        protected final GuiButtonExt btnAddNewEntryAbove;
        private final HoverChecker addNewEntryAboveHoverChecker;
        protected final GuiButtonExt btnRemoveEntry;
        private final HoverChecker removeEntryHoverChecker;
        private final List<String> addNewToolTip;
        private final List<String> removeToolTip;
        protected boolean isValidValue = true;
        protected boolean hasValidation;

        public BaseEntry(ModGuiEditArrayEntries owningEntryList, ConfigPropertyList configElement) {
            this.owningEntryList = owningEntryList;
            this.configElement = configElement;
            this.btnAddNewEntryAbove = new GuiButtonExt(0, 0, 0, 18, 18, "+");
            this.btnAddNewEntryAbove.packedFGColour = GuiUtils.getColorCode('2', true);
            this.btnAddNewEntryAbove.enabled = owningEntryList.isEnabled();
            this.btnRemoveEntry = new GuiButtonExt(0, 0, 0, 18, 18, "x");
            this.btnRemoveEntry.packedFGColour = GuiUtils.getColorCode('c', true);
            this.btnRemoveEntry.enabled = owningEntryList.isEnabled();
            this.addNewEntryAboveHoverChecker = new HoverChecker(this.btnAddNewEntryAbove, 800);
            this.removeEntryHoverChecker = new HoverChecker(this.btnRemoveEntry, 800);
            this.addNewToolTip = new ArrayList<>();
            this.removeToolTip = new ArrayList<>();
            addNewToolTip.add(I18n.format("fml.configgui.tooltip.addNewEntryAbove"));
            removeToolTip.add(I18n.format("fml.configgui.tooltip.removeEntry"));
            hasValidation = configElement.getElementType().hasValidation();
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partial) {
            if (this.getValue() != null && this.hasValidation)
                owningEntryList.getMC().fontRenderer.drawString(
                        isValidValue ? TextFormatting.GREEN + VALID : TextFormatting.RED + INVALID,
                        listWidth / 4 - owningEntryList.getMC().fontRenderer.getStringWidth(VALID) - 2,
                        y + slotHeight / 2 - owningEntryList.getMC().fontRenderer.FONT_HEIGHT / 2,
                        0xFFFFFF);

            int half = listWidth / 2;
            if (owningEntryList.canAddMoreEntries) {
                this.btnAddNewEntryAbove.visible = true;
                this.btnAddNewEntryAbove.x = half + ((half / 2) - 44);
                this.btnAddNewEntryAbove.y = y;
                this.btnAddNewEntryAbove.drawButton(owningEntryList.getMC(), mouseX, mouseY, partial);
            } else
                this.btnAddNewEntryAbove.visible = false;

            if (!configElement.isLengthFixed() && slotIndex != owningEntryList.listEntries.size() - 1) {
                this.btnRemoveEntry.visible = true;
                this.btnRemoveEntry.x = half + ((half / 2) - 22);
                this.btnRemoveEntry.y = y;
                this.btnRemoveEntry.drawButton(owningEntryList.getMC(), mouseX, mouseY, partial);
            } else
                this.btnRemoveEntry.visible = false;
        }

        @Override
        public void drawToolTip(int mouseX, int mouseY) {
            boolean canHover = mouseY < owningEntryList.bottom && mouseY > owningEntryList.top;
            if (this.btnAddNewEntryAbove.visible && this.addNewEntryAboveHoverChecker.checkHover(mouseX, mouseY, canHover))
                owningEntryList.drawToolTip(this.addNewToolTip, mouseX, mouseY);
            if (this.btnRemoveEntry.visible && this.removeEntryHoverChecker.checkHover(mouseX, mouseY, canHover))
                owningEntryList.drawToolTip(this.removeToolTip, mouseX, mouseY);
        }

        /**
         * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
         * clicked and the list should not be dragged.
         */
        @Override
        public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            if (this.btnAddNewEntryAbove.mousePressed(owningEntryList.getMC(), x, y)) {
                btnAddNewEntryAbove.playPressSound(owningEntryList.getMC().getSoundHandler());
                owningEntryList.addNewEntry(index);
                owningEntryList.recalculateState();
                return true;
            } else if (this.btnRemoveEntry.mousePressed(owningEntryList.getMC(), x, y)) {
                btnRemoveEntry.playPressSound(owningEntryList.getMC().getSoundHandler());
                owningEntryList.removeEntry(index);
                owningEntryList.recalculateState();
                return true;
            }

            return false;
        }

        /**
         * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
         */
        @Override
        public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
            this.btnAddNewEntryAbove.mouseReleased(x, y);
            this.btnRemoveEntry.mouseReleased(x, y);
        }

        @Override
        public void keyTyped(char eventChar, int eventKey) { }

        @Override
        public void updateCursorCounter() { }

        @Override
        public void mouseClicked(int x, int y, int mouseEvent) { }

        @Override
        public boolean isValueSavable() {
            return isValidValue;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void updatePosition(int p_178011_1_, int p_178011_2_, int p_178011_3_, float partial) { }
    }

    public interface IArrayEntry extends GuiListExtended.IGuiListEntry {
        void keyTyped(char eventChar, int eventKey);

        void updateCursorCounter();

        void mouseClicked(int x, int y, int mouseEvent);

        void drawToolTip(int mouseX, int mouseY);

        boolean isValueSavable();

        Object getValue();
    }
}