nutchHome="/opt/nutch-1.8"
export NUTCH_HOME=$nutchHome

path_content=$(echo $PATH)
pattern="$nutchHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi


