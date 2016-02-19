#!/bin/bash

date

server=$1

# path to tar.gz
mgt="/home/vagrant/subutai-tar/target"

# path to snappy build folder
snappy_repo="/home/vagrant/snappy-build"

package="subutai-4.0.0-RC6.tar.gz"
folder=`echo $package | awk -F ".tar" '{print $1}'`

rm -rf ./$folder
tar -xf $mgt/$package

files=(
'lib'
'system'
'deploy/webui-4.0.0-RC6.war'
'etc/subutai-mng/git.properties' 
'etc/subutai-mng/quota.cfg'
)

for file in "${files[@]}";
do 
	rm -rf $snappy_repo/subutai-mng/$file; 
	cp -rf $folder/$file $snappy_repo/subutai-mng/$file; 
done


# for file in 'lib' 'system' 'deploy/webui-4.0.0-RC6.war' 'etc/subutai-mng/git.properties' 'etc/subutai-mng/quota.cfg';
# do 
# 	rm -rf $snappy_repo/subutai-mng/$file; 
# 	cp -rf $folder/$file $snappy_repo/subutai-mng/$file; 
# done

pushd $snappy_repo
./autobuild.sh -t mng -b

if [[ -z $server ]];
then
	echo "Remote snappy is not specified, skipping..."
else
	cd ../
	snappy-remote --url=ssh://$server install export/snap/subutai-mng_4.0.0_amd64.snap
fi

popd

rm -rf ./$folder

echo

date
