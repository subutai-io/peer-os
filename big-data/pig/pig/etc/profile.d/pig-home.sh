pigHome="/opt/pig-0.13.0"
export PIG_HOME=$pigHome

path_content=$(echo $PATH)
pattern="$pigHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

