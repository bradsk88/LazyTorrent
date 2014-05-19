LazyTorrent
===========

***IMPORTANT***
This application was developed for experimental and educational purposes.  It is not intended to be used for the purposes of illegally downloading copyrighted or otherwise illegal materials. 

***Usage***
This application assumes the following: 

1) You are using Windows  
2) You have uTorrent installed at C:\Program Files (x86)\uTorrent\uTorrent.exe  
3) You have WinRAR installed and the command line application C:\Program Files\WinRAR\unrar.exe exists  
4) You have a hard drive labeled D:   
5) You have configured uTorrent to move the files from completed torrents to D:\Done  
6) You have a network device entitled \\XBMCBUNTU to which the files from completed torrents will be moved.   

Many of these requirements will be loosened eventually.  

When clicking the button "I want to download this" from the "Manual Control" pane, you are expected to reformat the file name so that future downloads will be matchable.   

For example:   
Select file "My.New.File.To.Download.S01.E10.COOLGUYS.xVid.US.TV"  
Click "I want to download this"  
In the new dialog, change the name to "My.New.File.To.Download"   
In the future, file like "My.New.File.To.Download.S02.E99.NOTCOOL.h264.REPACK" will automatically be downloaded and will be 
transferred to \\XBMCBUNTU\media_drive\TV\{{{THIS_YEAR}}}\My New File To Download.  
--Where {{{THIS_YEAR}}} will be the current year.  Eg: 2014.  
