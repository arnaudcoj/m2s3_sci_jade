compile:classes
	javac -classpath lib/jade.jar -d classes src/negociation/*.java

classes:
	mkdir classes

execute:compile
	java -cp lib/jade.jar:classes jade.Boot -gui -agents Creator:negociation.GenerationPetitMonde

clean:
	rm -rf classes/*
