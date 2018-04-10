package at.woodstick.pimutdroid.internal;

import com.android.build.gradle.api.BaseVariant;

public class AndroidVariant {

	private BaseVariant variant;

	public AndroidVariant(BaseVariant variant) {
		this.variant = variant;
	}

	public String getName() {
		return variant.getName();
	}

	public String getApplicationId() {
		return variant.getApplicationId();
	}

	public String getDirName() {
		return variant.getDirName();
	}

	public String getBuildTypeName() {
		return variant.getBuildType().getName();
	}

	public String getFlavorName() {
		return variant.getFlavorName();
	}

	public String getBaseName() {
		return variant.getBaseName();
	}

	@Override
	public String toString() {
		return "AndroidVariant [getName()=" + getName() + ", getApplicationId()=" + getApplicationId()
				+ ", getDirName()=" + getDirName() + ", getBuildTypeName()=" + getBuildTypeName() + ", getFlavorName()="
				+ getFlavorName() + ", getBaseName()=" + getBaseName() + "]";
	}
}
