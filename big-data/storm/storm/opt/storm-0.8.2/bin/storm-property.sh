#!/bin/bash

usage()
{
        echo "Usage: $0 {add|remove|refresh}"
        echo "Usage: $0 add fileName name_field value_field --> adds property with provided name and value fields(seperate multiple values with a comma) to file provided by fileName"
        echo "Usage: $0 remove fileName name_field --> removes provided property by name_field from provided file by fileName"
	echo "Usage: $0 refresh storm.xml--> Refreshes the content of yaml file according to xml file."
        echo "Example: $0 add storm.xml storm.zookeeper.servers server1,server2"
        echo "Example: $0 remove storm.xml storm.zookeeper.servers"
        exit 1
}
xmlFileInitialContent="<?xml version=\"1.0\"?>\n\n<!-- Put site-specific property overrides in this file. -->\n\n<configuration>\n\n</configuration>"
initializeFiles()
{
	if [ ! -d $stormConf ]; then
		mkdir -p $stormConf
	fi
	if [ -f $fileName ]; then
        	:
        else
		touch $fileName
		echo -e $xmlFileInitialContent >> $fileName
        fi
}
. /etc/profile
add_prop="";

if [[ $1 == "" || $2 == "" ]];
then
	usage
fi
stormConf="/etc/storm"
if [[ "x$STORM_CONF_DIR" != "x" ]];
then
        stormConf=$STORM_CONF_DIR
fi

fileName="$stormConf/$2"
yamlFile=$stormConf/storm.yaml
initializeFiles

do_add()
{
	escape_characters $name_field $value_field
        # Add the property if does not exist!
        if grep -q $name_field"<\/name>" $fileName
        then
                :
        else
                add_prop=$add_prop"\t<property>\n\t <name>$escaped_name<\/name>\n\t <value>$escaped_value<\/value>\n\t<\/property>\n"
        fi
        # Check if the file already has that property in it
        if [[ $add_prop == "" ]];
        then
                echo "Already has" $name_field "property! Changing the value of it to" $value_field
                configure
        else
                echo "Adding property with name:" $name_field "and value: " $value_field
                sed -i "s/<\/configuration>/$add_prop\n<\/configuration>/g" $fileName
        fi
}

configure()
{
        # Change the value of property if exists!
        pattern1="<name>([\s]*?)$escaped_name([\s]*?)<\/name>\n(.*?)<value>(.*?)<\/value>"
        pattern2="<name>$escaped_name<\/name>\n\t<value>$escaped_value<\/value>"
        #Edit configuration with $name_field in $fileName
        perl -0777 -i.original -pe "s/${pattern1}/${pattern2}/is" $fileName
}
escape_characters()
{
	temp_name=$1
	temp_value=$2
        #echo "Escaping some special characters like '/' for name:" $temp_name "and value:" $temp_value	
	escaped_name=$(echo $temp_name | sed -e 's/\//\\\//g')
	escaped_value=$(echo $temp_value | sed -e 's/\//\\\//g')
	#echo "Escaped Name:" $escaped_name ", Escaped Value:" $escaped_value  
}
refreshYamlFile()
{
	echo "Refreshing Yaml File"
	# Add each property exists in xml file to yaml file
	seperatorCharacter=";"
	valueSeperatorCharacter=","
	initializeNameList
	initializeValueList
	addNameValueToYaml
	#for var in "${nameList[@]}"
	#do
	#	echo ${#nameList[@]}
  	#	echo "${var}"
  	#	# do something on $var
	#done
}
addNameValueToYaml()
{
	IFS=';' read -a nameArray <<< "$extractedNameList"
	IFS=';' read -a valueArray <<< "$extractedValueList"
	# Confirm lengths of the two arrays are equal
	if [[ ${#nameArray[@]} -ne ${#valueArray[@]} ]]; then
		echo "Number of name and value fields do not match!"
		exit 1
	fi
	if [ -f $yamlFile ]; then
		echo "Clearing yaml file content" $yamlFile
		> $yamlFile
        else
		echo "Creating empty yaml file" $yamlFile
                touch $yamlFile
        fi
	#TODO implement inserting name and value pairs to yaml file for each case!
	for (( i=0 ; i < ${#nameArray[@]} ; i++ ))
	do
		detectName ${nameArray[$i]}
		addProperty ${nameArray[$i]} ${valueArray[$i]}
	done
}
addProperty()
{
	name=$1
	value=$2
	echo $name": hasQuotation:" $hasQuotation", hasDash:" $hasDash
	echo -n $name":" >> $yamlFile
	IFS=',' read -a values <<< "$value"
	shopt -s nocasematch
	echo "values:"
	echo ${values[@]}	
	if [[ $hasDash =~ (y|yes) ]] ; then
		for (( j=0 ; j < ${#values[@]} ; j++ ))
        	do
			if [[ $hasQuotation =~ (y|yes) ]] ; then
                        	echo -ne "\n     - \"${values[$j]}\"" >> $yamlFile
                	else
                        	echo -ne "\n     - $value" >> $yamlFile
                	fi
		done
	else
		for (( k=0 ; k < ${#values[@]} ; k++ ))
                do
                        if [[ $hasQuotation =~ (y|yes) ]] ; then
                                echo -n " \"${values[$k]}\"" >> $yamlFile
                        else
                                echo -n " $value" >> $yamlFile
                        fi
                done
	fi
	
#	shopt -s nocasematch
#	if [[ $hasQuotation =~ (y|yes) ]] ; then
#		#TODO split comma seperated values
#                echo -n "\"$value\"" >> $yamlFile
#        else
#		#TODO split comma seperated values
#                echo -n "$value" >> $yamlFile
#        fi
	echo "" >> $yamlFile

}
detectName()
{
	zookeeper_servers="storm.zookeeper.servers"
	nimbus_host="nimbus.host"
	local_dir="storm.local.dir"
	kryo_register="topology.kryo.register"
	kryo_decorators="topology.kryo.decorators"
	drpc_servers="drpc.servers"
	name=$1
	hasQuotation="Y";
	hasDash="N";
	if [ "${zookeeper_servers/$name}" != "$zookeeper_servers" ] ; then
#		echo $zookeeper_servers "found"
		hasQuotation="Y"
		hasDash="Y"
	elif [ "${nimbus_host/$name}" != "$nimbus_host" ] ; then
#		echo $nimbus_host "found"
		hasQuotation="Y"
                hasDash="N"
	elif [ "${local_dir/$name}" != "$local_dir" ]; then
#		echo $local_dir "found"
		hasQuotation="Y"
                hasDash="N"
	elif [ "${kryo_register/$name}" != "$kryo_register" ]; then
                hasQuotation="N"
                hasDash="Y"
	elif [ "${kryo_decorators/$name}" != "$kryo_decorators" ]; then
                hasQuotation="N"
                hasDash="Y"
	elif [ "${drpc_servers/$name}" != "$drpc_servers" ]; then
                hasQuotation="Y"
                hasDash="Y"
	else
		:
#		echo "This is not a defined name" $name"! Going with defaults..."
	fi
}
initializeNameList()
{
	tagName="name"
	# Find all of the tags
	tagList=$(xmllint --xpath "//configuration/property/$tagName" $fileName)
	#echo $tagName "tags:" $tagList
	# Replace starting tags
	extractedNameList=$(echo $tagList | sed "s/<$tagName>//g")
	# Replace closing tags with a seperatorCharacter
	extractedNameList=$(echo $extractedNameList | sed "s/<\/$tagName>/$seperatorCharacter/g")
	# Remove spaces
	extractedNameList=$(echo $extractedNameList | sed "s/\s//g")
	# Remove last extra seperatorCharacter put by an above expression
	extractedNameList=$(echo $extractedNameList | sed 's/\(.*\)'$seperatorCharacter'/\1/')
	
	echo $tagName "list:" $extractedNameList
	
}
initializeValueList()
{
	tagName="value"
        # Find all of the tags
        tagList=$(xmllint --xpath "//configuration/property/$tagName" $fileName)
        #echo $tagName "tags:" $tagList
        # Replace starting tags
        extractedValueList=$(echo $tagList | sed "s/<$tagName>//g")
        # Replace closing tags with a seperatorCharacter
        extractedValueList=$(echo $extractedValueList | sed "s/<\/$tagName>/$seperatorCharacter/g")
        # Remove spaces
        extractedValueList=$(echo $extractedValueList | sed "s/\s//g")
        # Remove last extra seperatorCharacter put by an above expression
        extractedValueList=$(echo $extractedValueList | sed 's/\(.*\)'$seperatorCharacter'/\1/')

        echo $tagName "list:" $extractedValueList
}
initializeVariables()
{
	if [ "x$1" = "x" ];
        then
                fileName=$stormConf/storm.xml
        else
		fileName=$stormConf/$1
	fi
        name_field=$2
}
case "$1" in

add)
        if [[ $2 == "" || $3 == "" || $4 == "" ]];
        then
                usage
        else
		initializeVariables $2 $3
                value_field=$4
		initializeFiles
                do_add
		refreshYamlFile
	fi
;;

remove)
	if [[ $2 == "" || $3 == "" ]];
        then
                usage
        else
		initializeVariables $2 $3
		initializeFiles
		escape_characters $name_field
        	pattern1="<property>([^\w]*?)<name>([\s]*?)$escaped_name([\s]*?)<\/name>([^\w]*?)<value>(.*?)<\/value>([^\w]*?)<\/property>"
        	pattern2="([\s]*?)<\/configuration>"
		pattern3="([\s]*?)<property>"
        	#Remove configuration with $name_field in $fileName
        	perl -0777 -i.original -pe "s/${pattern1}/""/is" $fileName
		#Remove empty before </configuration>
        	perl -0777 -i.original -pe "s/${pattern2}/\n<\/configuration>/is" $fileName
		#Remove empty lines before <property>
        	perl -0777 -i.original -pe "s/${pattern3}/\n\t<property>/isg" $fileName
		refreshYamlFile
	fi
;;
refresh)
	initializeVariables $2
	initializeFiles
	refreshYamlFile
;;
*)
        usage
esac
