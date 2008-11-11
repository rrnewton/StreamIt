#!/usr/uns/bin/python

import sys

print( 'Reading: ' + sys.argv[1] )
f = open( sys.argv[1] )
lines = f.readlines()
f.close()
print( 'Reading complete!' )

geometry = sys.argv[2]
g = geometry.split( 'x' )
width = int( g[0] )
height = int( g[1] )

print( 'size: ' + str( width ) + 'x' + str( height ) )

output_prefix = sys.argv[3]

red = []
green = []
blue = []

print( 'Allocating array...' )
red = [0] * ( width * height )
green = [0] * ( width * height )
blue = [0] * ( width * height )
print( 'Done!' )

print( 'Populating arrays...' )
i = 0
milestone = 10.0
while i < len( lines ):
    percent = ( 100.0 * i ) / ( len( lines ) )
    if percent > milestone:
        print( str( percent ) + "%" )
        milestone = milestone + 10
    l0 = lines[i].rstrip().split( ' ' )
    l1 = lines[i+1].rstrip().split( ' ' )
    l2 = lines[i+2].rstrip().split( ' ' )
    l3 = lines[i+3].rstrip().split( ' ' )
    l4 = lines[i+4].rstrip().split( ' ' )
    try:
        x = int( l0[2] )
        y = int( l1[2] )
        r = int( 255.0 * float( l2[2] ) )
        g = int( 255.0 * float( l3[2] ) )
        b = int( 255.0 * float( l4[2] ) )
        red[ y * width + x ] = r
        green[ y * width + x ] = g
        blue[ y * width + x ] = b
    except:
        print( 'lines[i] = ' + lines[i] )
        print( 'lines[i+1] = ' + lines[i + 1] )
        print( 'lines[i+2] = ' + lines[i + 2] )
        print( 'l0 = ' + str( l0 ) )
        print( 'l1 = ' + str( l1 ) )
        print( 'l2 = ' + str( l2 ) )
        sys.exit()
    i = i + 5
    
print( 'Done!' )

output_red_filename = output_prefix + '_red.arr'
output_green_filename = output_prefix + '_green.arr'
output_blue_filename = output_prefix + '_blue.arr'

print( 'Writing output arrays for pass 3:' )
print( output_red_filename )
print( output_green_filename )
print( output_blue_filename )

red_file = open( output_red_filename, 'w' )
green_file = open( output_green_filename, 'w' )
blue_file = open( output_blue_filename, 'w' )

i = 0.0
milestone = 10.0
for y in range( 0, height ):
    for x in range( 0, width ):
        percent = ( 100.0 * i ) / ( width * height )
        if percent > milestone:
            print( str( percent ) + "%" )
            milestone = milestone + 10
        red_file.write( str( red[ y * width + x ] / 255.0 ) + '\n' )
        green_file.write( str( green[ y * width + x ] / 255.0 ) + '\n' )
        blue_file.write( str( blue[ y * width + x ] / 255.0 ) + '\n' )
        i = i + 1

blue_file.close()
green_file.close()
red_file.close()
