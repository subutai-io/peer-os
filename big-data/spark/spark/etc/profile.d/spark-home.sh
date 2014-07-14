sparkHome="/opt/spark-1.0.0"
export SPARK_HOME=$sparkHome

path_content=$(echo $PATH)

pattern="$sparkHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

pattern="$sparkHome/sbin"
if [[ $path_content != *$pattern* ]];
then
        export PATH=$PATH:$pattern
fi

