# Microsoft Excel MuleSoft Anypoint Connector
Anypoint Connector for Microsoft Excel. This project leverages the Apache POI project to read in *.xlsx and *.xls files. You configure the location of the file and the sheet within the file to extract the data from. Optionally, you can choose to include or exclude the first row as headers. The connector reads the sheet and outputs the data into a List<Map> object.

<img src="https://raw.githubusercontent.com/djuang1/excel-connector/master/img/screenshot.png" width="500px">

# Example output

<img src="https://raw.githubusercontent.com/djuang1/excel-connector/master/img/example_output.png" width="500px">

# Mule supported versions
Mule 3.5.x

#Service or application supported modules
Microsoft Excel (XLS and XLSX)

# Installation 
For beta connectors you can download the source code and build it with devkit to find it available on your local repository. Then you can add it to Studio

For released connectors you can download them from the update site in Anypoint Studio. 
Open Anypoint Studio, go to Help → Install New Software and select Anypoint Connectors Update Site where you’ll find all avaliable connectors.

#Usage
For information about usage our documentation at http://github.com/djuang1/excel-connector.

# Reporting Issues

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link http://github.com/djuang1/excel-connector/issues.
