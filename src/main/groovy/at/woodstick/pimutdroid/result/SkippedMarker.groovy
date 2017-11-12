package at.woodstick.pimutdroid.result;

class SkippedMarker {
	private final boolean skipped;

	public SkippedMarker(boolean skipped) {
		this.skipped = skipped;
	}

	public boolean isSkipped() {
		return skipped;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (skipped ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SkippedMarker other = (SkippedMarker) obj;
		if (skipped != other.skipped)
			return false;
		return true;
	}

	public static SkippedMarker skipped() {
		return new SkippedMarker(true);
	}
	
	public static SkippedMarker notSkipped() {
		return new SkippedMarker(false);
	}
}
