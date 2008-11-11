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
eyeZ = [20.0] * ( width * height )
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
    l5 = lines[i+5].rstrip().split( ' ' )
    try:
        x = int( l0[2] )
        y = int( l1[2] )
        r = int( 255.0 * float( l2[2] ) )
        g = int( 255.0 * float( l3[2] ) )
        b = int( 255.0 * float( l4[2] ) )
        ez = float( l5[2] )
        red[ y * width + x ] = r
        green[ y * width + x ] = g
        blue[ y * width + x ] = b
        eyeZ[ y * width + x ] = ez
    except:
        print( 'lines[i] = ' + lines[i] )
        print( 'lines[i+1] = ' + lines[i + 1] )
        print( 'lines[i+2] = ' + lines[i + 2] )
        print( 'l0 = ' + str( l0 ) )
        print( 'l1 = ' + str( l1 ) )
        print( 'l2 = ' + str( l2 ) )
        sys.exit()
    i = i + 6
    
print( 'Done!' )

output_colorBuffer_ppm_filename = output_prefix + '_colorBuffer.ppm'
f = open( output_colorBuffer_ppm_filename, 'w' )
f.write( 'P3\n' )
f.write( str( width ) + ' ' + str( height ) + '\n' )
f.write( '255\n' )

print( 'Writing color buffer: ' + output_colorBuffer_ppm_filename + ' (for visualization)' )
i = 0.0;
milestone = 10.0
for y in range( 0, height ):
    for x in range( 0, width ):
        percent = ( 100 * i ) / ( width * height )
        if percent > milestone:
            print( str( percent ) + "%" )
            milestone = milestone + 10
        f.write( str( red[ ( height - y - 1 ) * width + x ] ) + ' ' )
        f.write( str( green[ ( height - y - 1 ) * width + x ] ) + ' ' )
        f.write( str( blue[ ( height - y - 1 ) * width + x ] ) + ' ' )
        i = i + 1
    f.write( '\n' )

f.close()

##########################
# write eye Z Buffer PPM
##########################

output_eyeZBuffer_filename = output_prefix + '_eyeZBuffer.ppm'
f = open( output_eyeZBuffer_filename, 'w' )
f.write( 'P3\n' )
f.write( str( width ) + ' ' + str( height ) + '\n' )
f.write( '255\n' )

# write eye Z Buffer
zMin = min( eyeZ )
zMax = max( eyeZ )

print( 'Writing eye zbuffer: ' + output_eyeZBuffer_filename + ' (for visualization)' )
i = 0.0;
milestone = 10.0
for y in range( 0, height ):
    for x in range( 0, width ):
        percent = ( 100 * i ) / ( width * height )
        if percent > milestone:
            print( str( percent ) + "%" )
            milestone = milestone + 10
        val = str( int( 255.0 * ( eyeZ[ ( height - y - 1 ) * width + x ] - zMin ) / ( zMax - zMin ) ) )
        f.write( val + ' ' )
        f.write( val + ' ' )
        f.write( val + ' ' )
        i = i + 1
    f.write( '\n' )

f.close()

##########################
# write output arr files
##########################

output_red_filename = 'Pass1_input_red.arr'
output_green_filename = 'Pass1_input_green.arr'
output_blue_filename = 'Pass1_input_blue.arr'
output_blurriness_filename = 'Pass3_input_blurriness.arr'

print( 'Writing colorBuffer arrays for pass 1:' )
print( output_red_filename )
print( output_green_filename )
print( output_blue_filename )
print( 'Writing blurriness array for pass 3:' )
print( output_blurriness_filename )

red_file = open( output_red_filename, 'w' )
green_file = open( output_green_filename, 'w' )
blue_file = open( output_blue_filename, 'w' )
blurriness_file = open( output_blurriness_filename, 'w' )

zNear = 1.0
zFocus = 10.0
zFar = 20.0

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
        z = -eyeZ[ y * width + x ];
        if z < zNear:
            z = zNear
        elif z > zFar:
            z = zFar
        blurriness = 0
        if z < zFocus:
            # zNear -> -1, zFocus -> 0
            blurriness = ( z - zFocus ) / ( zFocus - zNear )
        else:
            # zFar -> 1, zFocus -> 0
            blurriness = ( z - zFocus ) / ( zFar - zFocus )
        z = z * 0.5 + 0.5; # scale and bias to [0, 1]
        blurriness_file.write( str( blurriness ) + '\n' )
        i = i + 1

blurriness_file.close()
blue_file.close()
green_file.close()
red_file.close()
