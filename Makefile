VERSION=$(shell cat VERSION)

help:
	@echo
	@echo "Available goals are:"
	@echo
	@echo "    clean : Clean removes all artifacts from previous builds"
	@echo "      jar : Creates the lapps.jar file."
	@echo "  install : Copies the jar to the user's bin directory."
	@echo "     help : Displays this help message."
	@echo
	
jar:
	mvn package
	
clean:
	mvn clean
	
install:
	cp target/lsd-$(VERSION).jar $(HOME)/bin
	cat src/test/resources/lsd | sed 's/__VERSION__/$(VERSION)/' > $(HOME)/bin/lsd
	
debug:
	@echo "Current version is $(VERSION)"

