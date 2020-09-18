#!/bin/bash
set -e;
HOSTNAME=`hostname`

mongo localhost:27017/$MONGO_INITDB_DATABASE <<-EOF
    rs.initiate({
        _id: "repDB",
        members: [ { _id: 0, host: "${HOSTNAME}:27017" } ]
    });
    rs.slaveOk();
EOF
echo "Initiated replica set"

sleep 3

mongo localhost:27017/admin <<-EOF
  db.adminCommand( { setFeatureCompatibilityVersion: "4.0" } );
  db.adminCommand( { setParameter: 1, transactionLifetimeLimitSeconds: 120 } );
EOF

sleep 1

mongo localhost:27017 <<- EOF
  use $MONGO_INITDB_DATABASE;
  db.createCollection("delete_me");
  session=db.getMongo().startSession();
  delete_me=session.getDatabase("$MONGO_INITDB_DATABASE").delete_me
  delete_me.insertOne({"delete": true});
EOF

sleep 2




# a default non-root role
# MONGO_NON_ROOT_ROLE="${MONGO_NON_ROOT_ROLE:-readWrite}"

# if [ -n "${MONGO_NON_ROOT_USERNAME:-}" ] && [ -n "${MONGO_NON_ROOT_PASSWORD:-}" ]; then
# 	"${mongo[@]}" "$MONGO_INITDB_DATABASE" <<-EOJS
# 		db.createUser({
# 			user: $(_js_escape "$MONGO_NON_ROOT_USERNAME"),
# 			pwd: $(_js_escape "$MONGO_NON_ROOT_PASSWORD"),
# 			roles: [ { role: $(_js_escape "$MONGO_NON_ROOT_ROLE"), db: $(_js_escape "$MONGO_INITDB_DATABASE") } ]
# 			})
# 	EOJS
# else
# 	# print warning or kill temporary mongo and exit non-zero
# fi
