package common.typeutils;

public interface PropertyCodeGenerator {
	public String toCode(Property prop);

	public String toCode(BooleanProperty prop);

	public String toCode(ColorProperty prop);

	public String toCode(DirectoryProperty prop);

	public String toCode(DoubleProperty prop);

	public String toCode(EnumProperty prop);

	public String toCode(FileProperty prop);

	public String toCode(FontProperty prop);

	public String toCode(StringProperty prop);
}
