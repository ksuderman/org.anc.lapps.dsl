def string = "This is a string"

def it = new CharacterIterator(string)
while(it.hasNext()) {
	println it.next()
}

println "Done."
System.exit(0)

class CharacterIterator {
	String string
	int index = 0
	
	public CharacterIterator(String string) {
		this.string = string
		skipWhitespace()
	}
	
	boolean hasNext() {
		return index < string.length()
	}
	
	char next() {
		if (index < string.length()) {
			char ch = string[index]
			++index
			skipWhitespace()
			return ch
		}
		return null
	}
	
	private void skipWhitespace() {
		while (index < string.length() && Character.isWhitespace(string[index] as char)) {
			++index
		}
	}
}