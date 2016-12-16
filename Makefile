compile:
	javac -classpath lib/jade.jar -d classes src/negociation/*.java

execute:compile
	java -cp lib/jade.jar:classes jade.Boot -gui -agents Creator:negociation.GenerationPetitMonde

clean:
	rm -rf classes/*
