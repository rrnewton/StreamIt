%
% cookbook.mp: MetaPost diagrams for StreamIt Cookbook
% David Maze <dmaze@cag.lcs.mit.edu>
% $Id: cookbook.mp,v 1.1 2003-03-05 23:44:13 dmaze Exp $
%
% MetaPost is a drawing language based on Knuth's METAFONT that can
% produce embedded PostScript figures, with TeX strings embedded in it.
% It produces EPS files named cookbook.1, cookbook.2, ..., cookbook.n
% if you run 'mpost cookbook'.  Run 'texdoc mpintro' or 'texdoc mpman'
% for more infomration.
%

% Settings:
defaultfont := "rphvr";
defaultscale := 11pt/fontsize defaultfont;

% These macros probably want to be broken out into separate code, but...

% Basic unit length:
u=11pt;
% Standard block width:
fw=5u;

% Usage: height(p), width(p)
% Returns the height or width of the bounding box of p.
def height expr p = ypart (ulcorner bbox p - llcorner bbox p) enddef;
def width expr p = xpart (urcorner bbox p - ulcorner bbox p) enddef;
def ytop expr p = ypart ulcorner bbox p enddef;
def ybot expr p = ypart llcorner bbox p enddef;
def xleft expr p = xpart llcorner bbox p enddef;
def xright expr p = xpart lrcorner bbox p enddef;

% Returns p, but with name placed at its top left and a box around
% the whole thing.  Add o distance above/below.
vardef boxed(expr name, o)(suffix p) =
  addto p doublepath (ulcorner p+(-u,o))--(urcorner p+(u,o))--
    (lrcorner p+(u,-o))--(llcorner p+(-u,-o))--cycle withpen currentpen;
  addto p also thelabel.lrt(name, ulcorner p);
  p
enddef;

% Usage: filter("name")
% Returns a picture expression.
vardef filter(expr name) =
  save p; picture p; p=nullpicture;
  addto p doublepath (0,0)--(0,u)--(fw,u)--(fw,0)--cycle withpen currentpen;
  addto p also thelabel.lrt(name, (0,u));
  p
enddef;

% Usage:
%  draw toppipe("program", filter("foo"), filter("bar"));
% Returns a picture expression, drawing a pipeline without entry or
% exit arrows.
vardef toppipe(expr name)(text t) =
  save _h, _p, _q, _r;
  picture _p; _p=nullpicture;
  _h := 0;
  % What's actually going to get returned is a box with its top center at
  % 0,0.
  for v=t:
    picture _q; _q=v;
    if _h <> 0:
      addto _p contour makepath currentpen shifted (0,_h);
      addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
      addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
      _h := _h - u;
    fi
    picture _r; _r = _q shifted (-(width _q)/2-xleft _q,_h-ytop _q);
    addto _p also _r;
    _h := _h - height _r;
  endfor;
  boxed(name, u, _p)
enddef;
  
% Usage:
%  draw pipeline("pipe", filter("foo"), filter("bar"));
% Returns a picture expression.
vardef pipeline(expr name)(text t) =
  save _h, _p, _q, _r;
  picture _p; _p=nullpicture;
  _h := 0;
  % What's actually going to get returned is a box with its top center at
  % 0,0.
  for v=t:
    picture _q; _q=v;
    addto _p contour makepath currentpen shifted (0,_h);
    addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
    addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
    _h := _h - u;
    picture _r; _r = _q shifted (-(width _q)/2-xleft _q,_h-ytop _q);
    addto _p also _r;
    _h := _h - height _r;
  endfor;
  addto _p doublepath (0,_h)--(0,_h-u) withpen currentpen;
  addto _p contour arrowhead (0,_h)--(0,_h-u) withpen currentpen;
  _h := _h-u;
  boxed(name, 0, _p)
enddef;

% End of macro section.

beginfig(1);
  draw toppipe("Minimal",
    filter("IntSource"),
    filter("IntPrinter"));
endfig;

beginfig(2);
  draw toppipe("Minimal",
    filter("IntSource"),
    filter("Averager"),
    filter("IntPrinter"));
endfig;

end
