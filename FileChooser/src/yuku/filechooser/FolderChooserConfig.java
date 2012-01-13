package yuku.filechooser;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class FolderChooserConfig implements Parcelable {
	public static final String TAG = FolderChooserConfig.class.getSimpleName();

	public String title;
	public List<String> roots;
	public boolean showHidden;
	public boolean mustBeWritable;

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeStringList(roots);
		dest.writeByte((byte) (showHidden? 1: 0));
		dest.writeByte((byte) (mustBeWritable? 1: 0));
	}

	public static final Parcelable.Creator<FolderChooserConfig> CREATOR = new Parcelable.Creator<FolderChooserConfig>() {
		@Override public FolderChooserConfig[] newArray(int size) {
			return new FolderChooserConfig[size];
		}

		@Override public FolderChooserConfig createFromParcel(Parcel in) {
			FolderChooserConfig res = new FolderChooserConfig();
			res.title = in.readString();
			res.roots = new ArrayList<String>(); in.readStringList(res.roots);
			res.showHidden = in.readByte() != 0;
			res.mustBeWritable = in.readByte() != 0;
			return res;
		}
	};
}
