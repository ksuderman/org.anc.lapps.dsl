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
	cp target/lapps.jar $(HOME)/bin
	

	