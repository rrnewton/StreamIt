%
% cookbook.mp: MetaPost diagrams for StreamIt Cookbook
% David Maze <dmaze@cag.lcs.mit.edu>
% $Id: cookbook.mp,v 1.2 2003-03-06 19:43:39 dmaze Exp $
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

% Usage:
%  draw splitjoin("sj", "dup", "rr")
%    (filter("foo"), filter("bar"))("","")("1","2")
% ...where the parameters are name, split type, join type; children;
% splitter weights; joiner weights.  Returns a picture expression.
vardef splitjoin(expr name, split, join)(text children)(text sws)(text jws) =
  save _p, _h, _x, _i, _n, _w, _q, _r, _y, _xc, _path;
  picture _p; _p=nullpicture;
  path _path;
  % Draw each of the children.
  _i := 0; _w := 0;
  for v=children:
    _i := _i + 1;
    picture _q; _q=v;
    % Goal: left side at _w, vertical center at 0.
    picture _r; _r = _q shifted (_w-(xleft _q),-(ybot _q)-(height _q)/2);
    addto _p also _r;
    _h[_i] := (height _q) / 2;
    _x[_i] := _w + (width _q) / 2;
    _w := _w + (width _q) + u;
  endfor;
  _n := _i;
  % Draw arrows for splitter/joiner.
  _xc := ((xleft _p) + (xright _p)) / 2;
  _y := (ytop _p) + u;
  _i := 0;
  for v=sws:
    _i := _i + 1;
    exitif _i > _n;
    _path := (_xc,_y)--(_x[_i],_h[_i]);
    % NB: this assumes the linear path above.
    addto _p also thelabel.top(v, .3[(_x[_i],_h[_i]),(_xc,_y)]);
    addto _p doublepath _path withpen currentpen;
    addto _p contour arrowhead _path withpen currentpen;
  endfor;
  % Splitter, and entry arrow:
  addto _p also thelabel.top(split, (_xc,_y));
  addto _p doublepath (_xc,_y+2u)--(_xc,_y+u) withpen currentpen;
  addto _p contour arrowhead (_xc,_y+2u)--(_xc,_y+u) withpen currentpen;
  _y := (ybot _p) - u;
  _i := 0;
  for v=jws:
    _i := _i + 1;
    exitif _i > _n;
    _path := (_x[_i],-_h[_i])--(_xc,_y);
    % NB: this assumes the linear path above.
    addto _p also thelabel.bot(v, .3[(_x[_i],-_h[_i]),(_xc,_y)]);
    addto _p doublepath _path withpen currentpen;
    addto _p contour arrowhead _path withpen currentpen;
  endfor;
  % Joiner, and exit arrow:
  addto _p also thelabel.bot(join, (_xc,_y));
  addto _p doublepath (_xc,_y-u)--(_xc,_y-2u) withpen currentpen;
  addto _p contour arrowhead (_xc,_y-u)--(_xc,_y-2u) withpen currentpen;
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

beginfig(3);
  draw pipeline("BandPassFilter",
    splitjoin("BPFCore", "DUP", "RR")
    (filter("LowPass"),filter("LowPass"))("","")("1","1"),
    filter("Subtractor"));
endfig;

end
