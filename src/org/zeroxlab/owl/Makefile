JAVAC = javac
JPATH = org/zeroxlab/owl
FLAGS = -Djava.ext.dirs=/home/aitjcize/Work/aster/lib/javacv

all:
	$(JAVAC) $(FLAGS) *.java
	cd /home/aitjcize/Work/aster/src; \
	jar cmf $(JPATH)/manifest.txt $(JPATH)/owl.jar $(JPATH)/*.class $(JPATH)/test-images/*.png
	java $(FLAGS) -jar owl.jar

run:
	java $(FLAGS) -jar owl.jar

%.class: %.java
	$(JAVAC) $(FLAGS) $<

clean:
	rm *.class
