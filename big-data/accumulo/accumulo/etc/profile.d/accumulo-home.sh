accumuloHome="/opt/accumulo-1.4.2"
export ACCUMULO_HOME=$accumuloHome

path_content=$(echo $PATH)
pattern="$accumuloHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi
