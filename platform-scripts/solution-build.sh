# Change Directory to solution on local machine
echo $PWD
echo "Demo iDAAS Connect FHIR"
cd $PWD
cd ../

mvn clean install
echo "Maven Build Completed"
mvn package
echo "Maven Release Completed"
cd target
cp idaas-connect-acd-*.jar demo-idaas-connect-acd-fhir.jar
echo "Copied Release Specific Version to General version"
