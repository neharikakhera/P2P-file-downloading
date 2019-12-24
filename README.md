# P2P-file-downloading
Projects create a peer-to-peer network for file downloading.
The file owner designed as a server has a file divided into chunks and it listens on TCP port. The peer connects to file owner to download chunks and it itself has two thread of control one for upload neighbor and other for download neighbor.
