#! /bin/sh
echo 'ANN_S1'
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass=org.deepvgi.model.Training -Dexec.args="model.zip 30 45"
mvn exec:java -Dexec.mainClass=org.deepvgi.evaluation.Predicting -Dexec.args="model.zip image 0.97 2"


echo 'ANN_S2'

for i in 1 2 3 4 5
do
	echo "mapswipe train $i"
	cp src/main/resources/mapswipe_active_tiles/positive/batch$i\_* src/main/resources/vgi_tiles/positive/
	mvn clean
	mvn compile
	mvn exec:java -Dexec.mainClass=org.deepvgi.model.Training -Dexec.args="model_m_$i.zip 30 45"
	rm src/main/resources/vgi_tiles/positive/batch$i\_*
	mvn clean
	mvn compile
	echo "mapswipe result $i"
	mvn exec:java -Dexec.mainClass=org.deepvgi.evaluation.Predicting -Dexec.args="model_m_$i.zip image 0.97 2"
done

gap=1

for i in 1 2 3 4 5
do
	j=`expr $i + $gap`
	s=$i\_$j
	echo "uncertaity train $s"
	cp src/main/resources/uncertainty_active_tiles/batch$i\_* src/main/resources/vgi_tiles/negative/
	cp src/main/resources/uncertainty_active_tiles/batch$j\_* src/main/resources/vgi_tiles/negative/
	mvn clean
	mvn compile
	mvn exec:java -Dexec.mainClass=org.deepvgi.model.Training -Dexec.args="model_u_$s.zip 30 45"
	rm src/main/resources/vgi_tiles/negative/batch$i\_*
	rm src/main/resources/vgi_tiles/negative/batch$j\_*
	mvn clean
	mvn compile
	echo "uncertaity result $s"
	mvn exec:java -Dexec.mainClass=org.deepvgi.evaluation.Predicting -Dexec.args="model_u_$s.zip image 0.97 2"
done


for i in 1 2 3 4 5
do
	echo "uncertaity train $i"
	cp src/main/resources/uncertainty_active_tiles/batch$i\_* src/main/resources/vgi_tiles/negative/
	mvn clean
	mvn compile
	mvn exec:java -Dexec.mainClass=org.deepvgi.model.Training -Dexec.args="model_u_$i.zip 30 45"
	rm src/main/resources/vgi_tiles/negative/batch$i\_*
	mvn clean
	mvn compile
	echo "uncertaity result $i"
	mvn exec:java -Dexec.mainClass=org.deepvgi.evaluation.Predicting -Dexec.args="model_u_$i.zip image 0.97 2"
done

for i in 1 2 3 4 5
do
	j=`expr $i + $gap`
	s=$i\_$j
	echo "mapswipe train $s"
	cp src/main/resources/mapswipe_active_tiles/positive/batch$i\_* src/main/resources/vgi_tiles/positive/
	cp src/main/resources/mapswipe_active_tiles/positive/batch$j\_* src/main/resources/vgi_tiles/positive/
	mvn clean
	mvn compile
	mvn exec:java -Dexec.mainClass=org.deepvgi.model.Training -Dexec.args="model_m_$s.zip 30 45"
	rm src/main/resources/vgi_tiles/positive/batch$i\_*
	rm src/main/resources/vgi_tiles/positive/batch$j\_*
	mvn clean
	mvn compile
	echo "mapswipe result $s"
	mvn exec:java -Dexec.mainClass=org.deepvgi.evaluation.Predicting -Dexec.args="model_m_$s.zip image 0.97 2"
done
