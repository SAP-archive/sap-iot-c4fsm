echo "============== Step 1: Creating User Provided Service : fsm-backend ================="
cf create-user-provided-service fsm-backend -p fsm-backend.json

echo "============== Step 2: Creating HTML5 Application Repository Service Instance: fsm_html5_repo_runtime ================="
cf create-service html5-apps-repo app-runtime fsm_html5_repo_runtime

echo "============== Step 3: Building Sample HTML5 Application ================="
cd html5-app
java -jar <path-to-mta-archive-builder>\mta_archive_builder.jar --build-target=CF build

echo "============== Step 4: Deploying Sample HTML5 Application ================="
cf deploy html5-app

cd ../
echo "============== Step 5: Building FSM Proxy Application ================="
cd proxy
mvn clean install

echo "============== Step 6: Deploying Approuter Application ================="
cd ../
cf push fsm-approuter -f approuter/manifest.yml
echo "============== The basic setup of Approuter is done ================="
echo "============== Please perform below steps manually to setup Proxy application & Web Container ================="

echo "============== Step 7: Proxy Application Setup ================="
echo "============== a. Copy the generated fsm-approuter host URL ================="
echo "============== b. Update the same in destinations section of proxy/manifest.yml \n ================="
echo "============== c. Execute 'cf push -f proxy/manifest.yml' to push proxy application ================="
echo "============== d. Now the Proxy Application is ready to use ================="

echo "============== Step 8: Follow the documentation to configure the Web Container  ================="


