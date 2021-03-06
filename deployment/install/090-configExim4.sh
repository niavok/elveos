#!/bin/bash

if [ -z "$1" ] ; then

cat << EOF

$0: Install and configure the exim4 mail server.
-----------------------------

### Usage 

    $0 [ exec ] 
        Use the exec param to launch the script.

### Details

Exim will be configured to send all email he recieve using ou gmail account.
It is automatically sent mails, so we have to send them using the noreply account.
(email is noreply@linkeos.com)

We have to have a local mail server (For the mail reporting system etc.)
There are different possible choices, (procmail, qmail, sendmail, exim).

- Sendmail is old and complex (and not very secure)
- procmail is good but has a dependencie to python
- qmail is not realy maintained
- exim is the last one available... So I choose exim4.


/////////////////////////////////////
////////////////////////////////////
WARNING !!! 
UPDATE THIS SCRIPT USING:

- router/900_exim4-config_local_user

local_user_2:
  driver = dnslookup
  domains = +local_domains
  local_parts = ! root
  transport = remote_smtp
  cannot_route_message = Unknown user 2

- config :
dc_eximconfig_configtype='internet'
dc_other_hostnames=''
dc_local_interfaces='127.0.0.1 ; ::1'
dc_readhost='elveos.org'
dc_relay_domains=''
dc_minimaldns='false'
dc_relay_nets=''
dc_smarthost='smtp.gmail.com::587'
CFILEMODE='644'
dc_use_split_config='true'
dc_hide_mailname='false'
dc_mailname_in_oh='true'
dc_localdelivery='maildir_home'

//////////////////////////////////////
//////////////////////////////////////


TODO: A special transfer agent to automatically sign all mails.

EOF

elif [ "$1" = "exec" ] ; then
    ADDR_MAIL=noreply@linkeos.com

    # Install
    sudo apt-get install exim4

    # configure
    echo "Creating the default configuration file (update-exim4.conf.conf)"
    cat << EOF > /tmp/update-exim4.conf.conf
dc_eximconfig_configtype='smarthost'
dc_other_hostnames=''
dc_local_interfaces='127.0.0.1 ; ::1'
dc_readhost='elveos.org'
dc_relay_domains=''
dc_minimaldns='false'
dc_relay_nets=''
dc_smarthost='smtp.gmail.com::587'
CFILEMODE='644'
dc_use_split_config='true'
dc_hide_mailname='false'
dc_mailname_in_oh='true'
dc_localdelivery='maildir_home'
EOF
    sudo mv /tmp/update-exim4.conf.conf /etc/exim4/

    echo "Ensuite il faut ajouter dans le fichier /etc/exim4/passwd.client:
          gmail-smtp-msa.l.google.com:$ADDR_MAIL:votre_motDePasse"

    stty -echo
    read -p "Mot de passe pour $ADDR_MAIL: " _password
    echo
    stty echo

    sudo sh -c "echo \"gmail-smtp-msa.l.google.com:$ADDR_MAIL:$_password\" >> /etc/exim4/passwd.client"


    sudo mount -o remount,exec /var

    sudo dpkg-reconfigure exim4-config
    sudo update-exim4.conf
    sudo service exim4 restart

    sudo mount -o remount,noexec /var
fi
