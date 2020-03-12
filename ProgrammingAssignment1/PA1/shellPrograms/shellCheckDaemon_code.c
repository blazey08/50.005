#include "shellPrograms.h"

/*  A program that prints how many summoned daemons are currently alive */
int shellCheckDaemon_code()
{

   /* TASK 8 */
   //Create a command that trawl through output of ps -efj and contains "summond"
   char *command = malloc(sizeof(char) * 256);
   sprintf(command, "ps -efj | grep summond  | grep -v tty > output.txt");

   size_t size = 32;
   char *buffer = malloc(sizeof(char) * size);
   // TODO: Execute the command using system(command) and check its return value
   if(system(command) == -1){
       return 1;
   }
   free(command);

   int live_daemons = 0;
   // TODO: Analyse the file output.txt, wherever you set it to be. You can reuse your code for countline program
   // 1. Open the file
   FILE *fp = fopen("output.txt","r");
   // 2. Fetch line by line using getline()

   if(fp != NULL){
       while(getline(&buffer, &size, fp) != -1){
           live_daemons++;
       }
       fclose(fp);
       free(buffer);
   }
   // 3. Increase the daemon count whenever we encounter a line
   // 4. Close the file
   // 5. print your result

   if (live_daemons == 0)
      printf("No daemon is alive right now\n");
   else
   {
      printf("There are in total of %d live daemons \n", live_daemons);
   }


   // TODO: close any file pointers and free any statically allocated memory
   free(command);
   return 1;
}

int main(int argc, char **args)
{
   return shellCheckDaemon_code();
}
