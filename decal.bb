Function CreateDecalLayer(brush, num, tx#, ty#)
	t.triangle = Find_TriangleBySurface(num)
	
	If t\v1 <> t\v2 Then TU = t\u1 - t\u3
	If t\v1 <> t\v3 Then TU = t\u1 - t\u2
	If t\v2 <> t\v3 Then TU = t\u2 - t\u1
	DebugLog TU

	
	;decal_layer = CreateTexture(tx#,ty#)
	;SetBuffer TextureBuffer(decal_layer)
	;Color 255,255,255:Rect 0,0,TU,TV
	
	;SetBuffer BackBuffer()
	;ScaleTexture decal_layer,TU,TV
	;TextureBlend decal_layer,2
	;BrushTexture brush,decal_layer,0,2 ;Layer 2
	Return decal_layer
End Function