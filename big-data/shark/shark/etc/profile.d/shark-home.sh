sharkHome="/opt/shark-0.9.1"
export SHARK_HOME=$sharkHome

path_content=$(echo $PATH)
pattern="$sharkHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi

