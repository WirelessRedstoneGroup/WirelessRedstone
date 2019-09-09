cd tmp
dir | rename-item -NewName {$_.name -replace "stringsjson","strings"}