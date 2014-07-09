prestoHome="/opt/presto-server-0.73"
export PRESTO_HOME=$prestoHome

path_content=$(echo $PATH)
pattern="$prestoHome/bin"
if [[ $path_content != *$pattern* ]];
then
	export PATH=$PATH:$pattern
fi
