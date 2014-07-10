flumeHome="/opt/flume-1.5.0"
export FLUME_HOME=$flumeHome

path_content=$(echo $PATH)
pattern="$flumeHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

