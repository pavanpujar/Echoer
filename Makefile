#############################################################################
#   $ make all         compile and link all programs
#   $ make clean       clean objects and the executable file
##==========================================================================
CC := javac

all: Echoer

Echoer : 
	${CC} *.java
	@- echo "Compiled Successfully!!!"
        

##==========================================================================
clean:
	@- $(RM) *.class
	@- echo "Data Cleansing Done.Ready to Compile"


