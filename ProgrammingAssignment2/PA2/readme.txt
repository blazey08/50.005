How to:
    1) Ensure all .crt and .der files are in folder called certs
    2) Ensure all files to send are in folder called send
    3) compile ServerAPCP1
    4) run ServerAPCP1 <server cert name> <server private key name>
        Example: java ServerAPCP1 example.crt key.der
    5) compile and run ClientAPCP1
    6) wait for Client to authenticate server
    7) when > appears, type in commands

    To run AP + CP2 version, just use ServerAPCP2 and ClientAPCP2 instead

Commands available:
    send <filename> : sends file called <filename> to server
    del <filename>  : deletes file called <filename> from server
    dl <filename>   : download file called <filename> from server
    ls              : list files in server
    exit            : exit

Folder details:
    certs           : client side folder to store your certificate
    send            : client side folder to store files to send
    recv_recv_send  : client side folder to store downloaded files

    recv_certs      : server side folder to store received certificate
    recv_send       : server side folder to store uploaded files

Done by:
    Poh Shi Hui 1002921
    Chua Yong Teck 1003378