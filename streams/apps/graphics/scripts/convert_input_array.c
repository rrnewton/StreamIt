#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main( int argc, char* argv[] )
{
  char line[80];
  char* input_filename;
  char* output_filename;
  FILE* fIn;
  FILE* fOut;
  float val;
  int i;
  int j;

  if( argc < 3 )
    {
      fprintf( stderr, "Usage: convert_input_array input.txt output.bin\n" );
      exit( -1 );
    }
  
  input_filename = argv[1];
  output_filename = argv[2];
  fIn = fopen( input_filename, "r" );
  fOut = fopen( output_filename, "wb" );
  i = 0;
  while( fgets( line, 80, fIn ) != NULL )
    {
      sscanf( line, "%f\n", &val );
      printf( "val = %f\n", val );
      fwrite( &val, 4, 1, fOut );
      ++i;
    }
  printf( "%d floats written.\n", i );

  for( j = 0; j < 100; ++j )
    {
      fwrite( &val, 4, 1, fOut );
    }
  printf( "%d dummy floats written.\n", j );

  fclose( fOut );
  fclose( fIn );
}
