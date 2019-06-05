package io.sarl.csharpgenerator.tests.utilities;

@SuppressWarnings({ "javadoc", "nls" })
public final class MultilineStrings {
	private MultilineStrings() {}

	public static String toMultilineString(String... lines) {
		StringBuilder output = new StringBuilder();

		for (String line : lines)
			if (!(line = line.trim()).isEmpty())
				output.append(line).append('\n');

		return output.toString();
	}

	public static String fromMultilineString(String lines) {
		return toMultilineString(lines.split("[\r\n]+"));
	}
}
