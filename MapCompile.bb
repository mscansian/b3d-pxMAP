Include "worldcraft.bb"

Global Texture_Num% = 0

PXWC_LoadMap("Mapas/map3.map",1)
ReplaceTextures()
SaveMap("Mapas/map5.map")

Function ReplaceTextures()
	For s.surface = Each surface
		DebugLog "a"
		Img = LoadImage("Texturas/"+s\texture+".bmp")
		IW# = Float(ImageWidth(Img))
		IH# = Float(ImageHeight(Img))

		For t.triangle = Each triangle
			If t\surf_num = s\num
				DistU1# = Abs(t\u1 - t\u2)
				DistU2# = Abs(t\u3 - t\u2)
				DistU# = (DistU1*(DistU1>DistU2)) + (DistU2*(DistU2>DistU1))
				If (DistU1>DistU2) Then ValU = t\u1:Else:ValU = t\u3
				
				DistV1# = Abs(t\v1 - t\v2)
				DistV2# = Abs(t\v3 - t\v2)
				DistV# = (DistV1*(DistV1>DistV2)) + (DistV2*(DistV2>DistV1))
				If (DistV1>DistV2) Then ValV = t\v1:Else:ValV = t\v3
								
				Img2 = CreateImage(DistU*IW,DistV*IH)
				
				SetBuffer ImageBuffer(Img2)
				TileBlock Img,ValU*IW+s\shiftx,ValV*IH+s\shifty
				SetBuffer BackBuffer()
				
				SaveBuffer ImageBuffer(Img2),"Texturas2/"+s\texture$+"-"+texture_Num%+".bmp"
			EndIf
		Next
		
		s\texture$ = s\texture$+"-"+texture_Num%
		Texture_num = Texture_num + 1
	Next
End Function

Function SaveMap(loc$)
	file = WriteFile(loc$)
	For e.entity = Each entity
		WriteLine File,"{"
		
		For p.property = Each property
			If p\entity_num = e\num
				WriteLine File,Chr(34)+p\name+Chr(34)+" "+Chr(34)+p\value+Chr(34)
			EndIf
		Next
		
		For m.mesh = Each mesh
			If m\entity = e\num
				WriteLine File,"{"
				For t.triangle = Each triangle
					If t\mesh_num = m\num
						s.surface = Find_Surface(t\surf_num)
 						WriteLine File,"( "+Int(t\x1)+" "+Int(t\z1)+" "+Int(t\y1)+" ) ( "+Int(t\x2)+" "+Int(t\z2)+" "+Int(t\y2)+" ) ( "+Int(t\x3)+" "+Int(t\z3)+" "+Int(t\y3)+" ) "+s\texture$+" [ "+Int(t\tv1x)+" "+Int(t\tv1y)+" "+Int(t\tv1z)+" 0 ] [ "+Int(t\tv2x)+" "+Int(t\tv2y)+" "+Int(t\tv2z)+" 0 ] "+Int(s\rotation)+" 1 1 "
					EndIf
				Next
				WriteLine File,"}"
			EndIf
		Next
		
		WriteLine File,"}"
	Next
	CloseFile file
End Function