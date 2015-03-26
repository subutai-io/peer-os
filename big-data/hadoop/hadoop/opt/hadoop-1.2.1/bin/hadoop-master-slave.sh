set -e

function usage()
{
  echo "Example usage:"
  echo "$0 masters/slaves clear -> Clears the content of the masters/slaves file"
  echo "$0 masters/slaves clear localhost -> Removes localhost from masters/slaves file"
  echo "$0 masters/slaves localhost -> Adds localhost to masters/slaves file"
  echo "$0 masters/slaves rollback -> Takes the last modification made on the masters/slaves file back"
  exit 0
}
if [[ "$1" == "" ]]; then
  usage
fi

if [[ "$1" == "help" ]]; then
  usage
fi

. /etc/profile

hadoopConf="/etc/hadoop"
if [[ "x$HADOOP_CONF_DIR" != "x" ]]; then
  hadoopConf=$HADOOP_CONF_DIR
fi

file="$hadoopConf/${1}"

if [[ "$2" == "rollback" ]]; then
  if [ -f "${file}.original" ]; then
    rm $file
    mv "${file}.original" ${file}
  fi
  exit 0
fi

#Keep a backup file of the original one
if [ -f "${file}.original" ]; then
  rm "${file}.original"
fi
cp -a ${file} "${file}.original"

#Clear the whole file or remove one machine from the masters/slaves file
if [[ "$2" == "clear" ]];
then
  #Remove the whole file
  if [[ "$3" == "" ]]; then
    cat /dev/null > $file
    exit 0
  fi
  #Else remove the specific machine from the masters/slaves file
  sed -i "/$3/d" $file
  exit 0
#or just add the machine to the masters/slaves file
else
  if [[ -z `cat $file | grep $2` ]]; then
     echo $2 >> $file
  fi 
fi 
