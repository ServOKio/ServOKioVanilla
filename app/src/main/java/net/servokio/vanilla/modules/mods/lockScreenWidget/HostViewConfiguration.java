package net.servokio.vanilla.modules.mods.lockScreenWidget;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.core.view.GravityCompat;

import java.util.List;

public class HostViewConfiguration implements Parcelable {
    public static final int MIN_HEIGHT = 101;
    public static final int MIN_WIDTH = 100;
    private int backgroundColor;
    private int bottomMargin;
    private int bottomPadding;
    private int clickType;
    private int customHeight;
    private int customWidth;
    private int elevation;
    private int gravity;
    private int height;
    private int index;
    private boolean isClickable;
    private int leftMargin;
    private int leftPadding;
    private int previewId;
    private int rightMargin;
    private int rightPadding;
    private float rotateX;
    private float rotateY;
    private float rotateZ;
    private float scaleX;
    private float scaleY;
    private int topMargin;
    private int topPadding;
    private float translateX;
    private float translateY;
    private boolean unlockWhenClicked;
    private boolean useBackColorElevation;
    private boolean useCustomHeight;
    private boolean useCustomMargin;
    private boolean useCustomPadding;
    private boolean useCustomWidth;
    private int width;
    private int xposedId;
    public static final String[] widthWidgetStock = {"Wrap content", "Match parent", "Min width"};
    public static final String[] heightWidgetStock = {"Wrap content", "Min height"};
    public static final String[] gravities = {"Start", "Center horizontal", "End"};
    public static final int[] widthWidgetStockConverter = {-2, -1, 100};
    public static final int[] heightWidgetStockConverter = {-2, 101};
    public static final int[] gravitiesConverter = {8388611, 1, GravityCompat.END};
    public static final Parcelable.Creator<HostViewConfiguration> CREATOR = new Parcelable.Creator<HostViewConfiguration>() { // from class: com.ssrdroide.lockscreenwidgets.model.HostViewConfiguration.1
        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public HostViewConfiguration createFromParcel(Parcel in) {
            return new HostViewConfiguration(in);
        }

        /* JADX WARN: Can't rename method to resolve collision */
        @Override // android.os.Parcelable.Creator
        public HostViewConfiguration[] newArray(int size) {
            return new HostViewConfiguration[size];
        }
    };

    public HostViewConfiguration(int previewId, int xposedId, int index, boolean isClickable, int clickType, boolean unlockWhenClicked, int width, boolean useCustomWidth, int customWidth, int height, boolean useCustomHeight, int customHeight, float translateX, float translateY, float scaleX, float scaleY, float rotateX, float rotateY, float rotateZ, int gravity, boolean useCustomMargin, int leftMargin, int topMargin, int rightMargin, int bottomMargin, boolean useCustomPadding, int leftPadding, int topPadding, int rightPadding, int bottomPadding, boolean useBackColorElevation, int backgroundColor, int elevation) {
        this.previewId = previewId;
        this.xposedId = xposedId;
        this.index = index;
        this.isClickable = isClickable;
        this.clickType = clickType;
        this.unlockWhenClicked = unlockWhenClicked;
        this.width = width;
        this.useCustomWidth = useCustomWidth;
        this.customWidth = customWidth;
        this.height = height;
        this.useCustomHeight = useCustomHeight;
        this.customHeight = customHeight;
        this.translateX = translateX;
        this.translateY = translateY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.gravity = gravity;
        this.useCustomMargin = useCustomMargin;
        this.leftMargin = leftMargin;
        this.topMargin = topMargin;
        this.rightMargin = rightMargin;
        this.bottomMargin = bottomMargin;
        this.useCustomPadding = useCustomPadding;
        this.leftPadding = leftPadding;
        this.topPadding = topPadding;
        this.rightPadding = rightPadding;
        this.bottomPadding = bottomPadding;
        this.useBackColorElevation = useBackColorElevation;
        this.backgroundColor = backgroundColor;
        this.elevation = elevation;
    }

    public HostViewConfiguration(int previewId, int xposedId, int index) {
        this.previewId = previewId;
        this.xposedId = xposedId;
        this.index = index;
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.gravity = 1;
        this.backgroundColor = 0;
    }

    public HostViewConfiguration(int previewId) {
        this.previewId = previewId;
    }

    public HostViewConfiguration() {
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
    }

    public boolean equals(Object o) {
        return o != null && (o instanceof HostViewConfiguration) && ((HostViewConfiguration) o).getPreviewId() == this.previewId;
    }

    public String toString() {
        String aux = "PreviewId: " + this.previewId + "\nXposedId: " + this.xposedId + "\nIndex: " + this.index + "\nIsClickable: " + this.isClickable + "\nClickType: " + this.clickType + "\nunlockWhenClicked: " + this.unlockWhenClicked + "\nWidth: " + this.width + "\nUseCustomWidth: " + this.useCustomWidth + "\nCustomWidth: " + this.customWidth + "\nHeight: " + this.height + "\nUseCustomHeight: " + this.useCustomHeight + "\nCustomHeight: " + this.customHeight + "\nTranslateX: " + this.translateX + "\nTranslateY: " + this.translateY + "\nScaleX: " + this.scaleX + "\nScaleY: " + this.scaleY + "\nrotateX: " + this.rotateX + "\nrotateY: " + this.rotateY + "\nrotateZ: " + this.rotateZ + "\nGravity: " + this.gravity + "\nUseCustomMargin: " + this.useCustomMargin + "\nLeftMargin: " + this.leftMargin + "\nTopMargin: " + this.topMargin + "\nRightMargin: " + this.rightMargin + "\nBottomMargin: " + this.bottomMargin + "\nUseCustomPadding: " + this.useCustomPadding + "\nLeftPadding: " + this.leftPadding + "\nTopPadding: " + this.topPadding + "\nRightPadding: " + this.rightPadding + "\nBottomPadding: " + this.bottomPadding + "\nuseBackColorElevation: " + this.useBackColorElevation + "\nbackgroundColor: " + this.backgroundColor + "\nelevation: " + this.elevation;
        return aux;
    }

    public String toStringAux() {
        String aux = "\tPreviewId: " + this.previewId + "\n\tXposedId: " + this.xposedId + "\n\tIndex: " + this.index + "\n\tIsClickable: " + this.isClickable + "\n\tClickType: " + this.clickType + "\n\tunlockWhenClicked: " + this.unlockWhenClicked + "\n\tWidth: " + this.width + "\n\tUseCustomWidth: " + this.useCustomWidth + "\n\tCustomWidth: " + this.customWidth + "\n\tHeight: " + this.height + "\n\tUseCustomHeight: " + this.useCustomHeight + "\n\tCustomHeight: " + this.customHeight + "\n\tTranslateX: " + this.translateX + "\n\tTranslateY: " + this.translateY + "\n\tScaleX: " + this.scaleX + "\n\tScaleY: " + this.scaleY + "\n\trotateX: " + this.rotateX + "\n\trotateY: " + this.rotateY + "\n\trotateZ: " + this.rotateZ + "\n\tGravity: " + this.gravity + "\n\tUseCustomMargin: " + this.useCustomMargin + "\n\tLeftMargin: " + this.leftMargin + "\n\tTopMargin: " + this.topMargin + "\n\tRightMargin: " + this.rightMargin + "\n\tBottomMargin: " + this.bottomMargin + "\n\tUseCustomPadding: " + this.useCustomPadding + "\n\tLeftPadding: " + this.leftPadding + "\n\tTopPadding: " + this.topPadding + "\n\tRightPadding: " + this.rightPadding + "\n\tBottomPadding: " + this.bottomPadding + "\n\tuseBackColorElevation: " + this.useBackColorElevation + "\n\tbackgroundColor: " + this.backgroundColor + "\n\televation: " + this.elevation;
        return aux;
    }

    public static void logListOfItems(List<HostViewConfiguration> widgetsConfigurationList) {
        if (widgetsConfigurationList == null) {
            Log.e("savedInstanceState", "logListOfItems -> precondition failed");
            return;
        }
        Log.i("savedInstanceState", "logListOfItems...");
        if (widgetsConfigurationList.isEmpty()) {
            Log.i("savedInstanceState", "logListOfItems -> list empty");
        } else {
            Log.i("savedInstanceState", "number of items -> " + widgetsConfigurationList.size());
        }
        for (HostViewConfiguration temp : widgetsConfigurationList) {
            Log.i("tag", "item: " + temp.toString());
        }
    }

    protected HostViewConfiguration(Parcel in) {
        boolean z = true;
        this.previewId = in.readInt();
        this.xposedId = in.readInt();
        this.index = in.readInt();
        this.isClickable = in.readByte() != 0;
        this.clickType = in.readInt();
        this.unlockWhenClicked = in.readByte() != 0;
        this.width = in.readInt();
        this.useCustomWidth = in.readByte() != 0;
        this.customWidth = in.readInt();
        this.height = in.readInt();
        this.useCustomHeight = in.readByte() != 0;
        this.customHeight = in.readInt();
        this.translateX = in.readFloat();
        this.translateY = in.readFloat();
        this.scaleX = in.readFloat();
        this.scaleY = in.readFloat();
        this.rotateX = in.readFloat();
        this.rotateY = in.readFloat();
        this.rotateZ = in.readFloat();
        this.gravity = in.readInt();
        this.useCustomMargin = in.readByte() != 0;
        this.leftMargin = in.readInt();
        this.topMargin = in.readInt();
        this.rightMargin = in.readInt();
        this.bottomMargin = in.readInt();
        this.useCustomPadding = in.readByte() != 0;
        this.leftPadding = in.readInt();
        this.topPadding = in.readInt();
        this.rightPadding = in.readInt();
        this.bottomPadding = in.readInt();
        this.useBackColorElevation = in.readByte() == 0 ? false : z;
        this.backgroundColor = in.readInt();
        this.elevation = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        int i = 1;
        dest.writeInt(this.previewId);
        dest.writeInt(this.xposedId);
        dest.writeInt(this.index);
        dest.writeByte((byte) (this.isClickable ? 1 : 0));
        dest.writeInt(this.clickType);
        dest.writeByte((byte) (this.unlockWhenClicked ? 1 : 0));
        dest.writeInt(this.width);
        dest.writeByte((byte) (this.useCustomWidth ? 1 : 0));
        dest.writeInt(this.customWidth);
        dest.writeInt(this.height);
        dest.writeByte((byte) (this.useCustomHeight ? 1 : 0));
        dest.writeInt(this.customHeight);
        dest.writeFloat(this.translateX);
        dest.writeFloat(this.translateY);
        dest.writeFloat(this.scaleX);
        dest.writeFloat(this.scaleY);
        dest.writeFloat(this.rotateX);
        dest.writeFloat(this.rotateY);
        dest.writeFloat(this.rotateZ);
        dest.writeInt(this.gravity);
        dest.writeByte((byte) (this.useCustomMargin ? 1 : 0));
        dest.writeInt(this.leftMargin);
        dest.writeInt(this.topMargin);
        dest.writeInt(this.rightMargin);
        dest.writeInt(this.bottomMargin);
        dest.writeByte((byte) (this.useCustomPadding ? 1 : 0));
        dest.writeInt(this.leftPadding);
        dest.writeInt(this.topPadding);
        dest.writeInt(this.rightPadding);
        dest.writeInt(this.bottomPadding);
        if (!this.useBackColorElevation) {
            i = 0;
        }
        dest.writeByte((byte) i);
        dest.writeInt(this.backgroundColor);
        dest.writeInt(this.elevation);
    }

    public int getPreviewId() {
        return this.previewId;
    }

    public void setPreviewId(int previewId) {
        this.previewId = previewId;
    }

    public int getXposedId() {
        return this.xposedId;
    }

    public void setXposedId(int xposedId) {
        this.xposedId = xposedId;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isClickable() {
        return this.isClickable;
    }

    public void setIsClickable(boolean isClickable) {
        this.isClickable = isClickable;
    }

    public int getClickType() {
        return this.clickType;
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }

    public boolean isUnlockWhenClicked() {
        return this.unlockWhenClicked;
    }

    public void setUnlockWhenClicked(boolean unlockWhenClicked) {
        this.unlockWhenClicked = unlockWhenClicked;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isUseCustomWidth() {
        return this.useCustomWidth;
    }

    public void setUseCustomWidth(boolean useCustomWidth) {
        this.useCustomWidth = useCustomWidth;
    }

    public int getCustomWidth() {
        return this.customWidth;
    }

    public void setCustomWidth(int customWidth) {
        this.customWidth = customWidth;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isUseCustomHeight() {
        return this.useCustomHeight;
    }

    public void setUseCustomHeight(boolean useCustomHeight) {
        this.useCustomHeight = useCustomHeight;
    }

    public int getCustomHeight() {
        return this.customHeight;
    }

    public void setCustomHeight(int customHeight) {
        this.customHeight = customHeight;
    }

    public float getTranslateX() {
        return this.translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
    }

    public float getTranslateY() {
        return this.translateY;
    }

    public void setTranslateY(float translateY) {
        this.translateY = translateY;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public float getRotateX() {
        return this.rotateX;
    }

    public void setRotateX(float rotateX) {
        this.rotateX = rotateX;
    }

    public float getRotateY() {
        return this.rotateY;
    }

    public void setRotateY(float rotateY) {
        this.rotateY = rotateY;
    }

    public float getRotateZ() {
        return this.rotateZ;
    }

    public void setRotateZ(float rotateZ) {
        this.rotateZ = rotateZ;
    }

    public int getGravity() {
        return this.gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public boolean isUseCustomMargin() {
        return this.useCustomMargin;
    }

    public void setUseCustomMargin(boolean useCustomMargin) {
        this.useCustomMargin = useCustomMargin;
    }

    public int getLeftMargin() {
        return this.leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getTopMargin() {
        return this.topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    public int getRightMargin() {
        return this.rightMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
    }

    public int getBottomMargin() {
        return this.bottomMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public boolean isUseCustomPadding() {
        return this.useCustomPadding;
    }

    public void setUseCustomPadding(boolean useCustomPadding) {
        this.useCustomPadding = useCustomPadding;
    }

    public int getLeftPadding() {
        return this.leftPadding;
    }

    public void setLeftPadding(int leftPadding) {
        this.leftPadding = leftPadding;
    }

    public int getTopPadding() {
        return this.topPadding;
    }

    public void setTopPadding(int topPadding) {
        this.topPadding = topPadding;
    }

    public int getRightPadding() {
        return this.rightPadding;
    }

    public void setRightPadding(int rightPadding) {
        this.rightPadding = rightPadding;
    }

    public int getBottomPadding() {
        return this.bottomPadding;
    }

    public void setBottomPadding(int bottomPadding) {
        this.bottomPadding = bottomPadding;
    }

    public boolean isUseBackColorElevation() {
        return this.useBackColorElevation;
    }

    public void setUseBackColorElevation(boolean useBackColorElevation) {
        this.useBackColorElevation = useBackColorElevation;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getElevation() {
        return this.elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

}
