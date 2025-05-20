#!/bin/bash

set -e 

version=$1
package=`ls playreact-${version}.tar.gz`
package_folder=`tar tf ${package} | sed -e 's@/.*@@' | uniq`
install_path=/opt/userplay

echo "Create necessary directories"
mkdir -p ${install_path}/releases/$version
mkdir -p ${install_path}/swamper_targets
mkdir -p ${install_path}/data
mkdir -p ${install_path}/third-party

echo "Unpacking package ${package} inside ${package_folder}"
tar -xvf $package

echo "Create Devops and playreact directories"
mkdir -p ${package_folder}/devops
mkdir -p ${package_folder}/playreact

if [ -L ${install_path}/playreact ]; then
 echo "Renaming current symlink"
 mv ${install_path}/playreact ${install_path}/playreact_backup
fi
if [ -L ${install_path}/devops ]; then
 echo "Renaming current symlink"
 mv ${install_path}/devops ${install_path}/devops_backup
fi

echo "Creating symlinks"
ln -s $(pwd)/${package_folder}/playreact ${install_path}/playreact
ln -s $(pwd)/${package_folder}/devops ${install_path}/devops


