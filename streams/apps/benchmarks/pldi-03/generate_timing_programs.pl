#!/usr/local/bin/perl
# script that generates programs to time for results of frequencyreplacement

use strict;
require "reaplib.pl";
my $OUTPUTDIR = "timing";

if (not @ARGV) {
    print "usage: do_timing_programs.pl inputscript\n";
    print "  input script has on separate lines: postfix:options:filename\n";
    die();
}
# input is "postfix:options:filename"
# select the tests to run
my @input_lines = split("\n", read_file(shift(@ARGV)));

# make sure that we have the benchmarks ready to go
print `make benchmarks`;
print `mkdir -p $OUTPUTDIR`;

#loop on the tests
foreach (@input_lines) {
    my ($postfix, $options, $filename) = split(":");
    
    # if the file already exists, then skip
    if (-e "$OUTPUTDIR/$filename-$postfix.c") {
	print "$filename-$postfix.c: already exists\n";
	next;
    }

    print "$filename-$postfix:";
    # copy the file
    print `cp $filename.java $OUTPUTDIR/$filename.java`;
    
    #java->c
    do_streamit_compile($OUTPUTDIR, $filename, $options);
    print `cp $OUTPUTDIR/$filename.c $OUTPUTDIR/$filename-$postfix.c`;
    # normal c->exe
    do_c_compile($OUTPUTDIR, "$filename-$postfix");

    # copy to a "np" = no print file
    print `cp $OUTPUTDIR/$filename-$postfix.c $OUTPUTDIR/$filename-$postfix-np.c`;
    # remove prints
    remove_prints($OUTPUTDIR, "$filename-$postfix-np");

    # noprint c->exe
    do_c_compile($OUTPUTDIR, "$filename-$postfix-np");

    # and we are done
    print "(done)\n";
}
