Function ApplyShading(brush, num)
	t.triangle = Find_TriangleBySurface(num)
	If t\v1 = t\v2 Then TU# = (t\u1-t\u2)
	If t\v1 = t\v3 Then TU# = (t\u1-t\u3)
	If t\v3 = t\v2 Then TU# = (t\u3-t\u2)
	
	If t\u1 = t\u2 Then TV# = (t\v1-t\v2)
	If t\u1 = t\u3 Then TV# = (t\v1-t\v3)
	If t\u3 = t\u2 Then TV# = (t\v3-t\v2)
		
	sha = LoadTexture("shadow/"+num+".bmp")
	ScaleTexture sha,TU,TV
	TextureBlend sha,2
	BrushTexture brush,sha,0,3 ;Layer 3
End Function