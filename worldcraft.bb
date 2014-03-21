Include "shader.bb"
Include "decal.bb"

;Variaveis par ao Jogo
Global TextureRoot$ = "Texturas/"

;SETA VARIAVEIS
Global LAST_EXPLODE_CHAR%
Global MESH_COUNT%
Global SURFACE_COUNT%
Global TRIANGLE_COUNT%
Global TEXTURE_COUNT%
Global ENTITY_COUNT%

;Opções para debug
Const MAP_SCALE#       = 10
Const FLAT_TEXTURE     = 0
Const RENDER_FILE_ONLY = 0
Const Both_Sides       = 0

;Seta Types
Type Entity
	Field num%
End Type

Type Property
	Field entity_num, name$, value$
End Type

Type Mesh
	Field hand%, num%, entity%
End Type

Type Surface
	Field hand%,mesh_num%, num%
	Field Tris.Triangle
	Field texture$, shiftx#, shifty#, scalex#, scaley#, rotation#
	Field texture_hand%, IW#, IH#
End Type

Type Texture
	Field name$, hand%, shiftx#, shifty#, scalex#, scaley#, rotation#, brush, num%, surf_num%
	Field decal_layer%
End Type

Type Triangle
	Field Surf_num%, num%, Mesh_num%, hand%
	Field x1#,x2#,x3#
	Field y1#,y2#,y3#
	Field z1#,z2#,z3#
	Field tv1x#,tv2x# ;Ponto X do vetor de textura
	Field tv1y#,tv2y# ;Ponto Y do vetor de textura
	Field tv1z#,tv2z#
	Field u1#,u2#,u3#
	Field v1#,v2#,v3#
	Field varx1,vary1,varz1
	Field On_File%
End Type
;FIM SETA VARIAVEIS

;CARREGA MAPA

Function PXWC_LoadMap(Map_Filename$,Only_File=0)
	Map_File = ReadFile (Map_Filename$)
	Brack_Open = 0
	File_Line_Last$ = ""
	
	While Not Eof(Map_File)
		File_Line$ = ReadLine(Map_File)

		If Brack_Open = 0
			If Left(File_Line$,1) = "{" Then 
				Brack_Open = Brack_Open + 1 ;Abre chaves
			EndIf
		Else
			If Left(File_Line$,11) = Chr(34)+"classname"+Chr(34) Then ;Novo Mesh
				If Left(File_Line_Last$,1) = "{" Then
					;Cria Entidade
					e.entity = New entity
					e\num = ENTITY_COUNT%
					ENTITY_COUNT% = ENTITY_COUNT% + 1
					p.property = New property
					p\entity_num = e\num
					p\name$ = "classname"
					p\value$ = Explode(File_Line$,13,Chr(34))
				EndIf
			ElseIf Left(File_Line$,1) = Chr(34) Then ;Nova Propriedade
				p.property = New property
				p\entity_num = e\num
				p\name$ = Lower(Explode(File_Line$,1,Chr(34)))
				p\value$ = Explode(File_Line$,LAST_EXPLODE_CHAR%+3,Chr(34))
			ElseIf Left(File_Line$,2) = "( " Then ;Novo Triangulo
				If Left(File_Line_Last$,1) = "{" Then
					;Cria Mesh
					m.mesh = New Mesh
					m\num = MESH_COUNT%
					m\entity = e\num
					MESH_COUNT% = MESH_COUNT% + 1
				EndIf
			
				;Cria Superficie
				s.surface = New surface
				s\num = SURFACE_COUNT%
				s\mesh_num% = m\num
				SURFACE_COUNT% = SURFACE_COUNT% + 1
				
				;Cria Triangulo
				t.Triangle = New Triangle
				t\num = TRIANGLE_COUNT%
				t\surf_num = s\num
				t\mesh_num% = s\mesh_num%
				TRIANGLE_COUNT% = TRIANGLE_COUNT% + 1

				t\x1# = Float(Explode(File_Line$,2," "))/1
				t\z1# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
				t\y1# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
				t\x2# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+5," "))/1
				t\z2# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
				t\y2# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
				t\x3# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+5," "))/1
				t\z3# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
				t\y3# = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))/1
			
				;Salva Texturas
				s\texture = Explode(File_Line$,LAST_EXPLODE_CHAR%+3," ")
				t\tv1x = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+3," "))
				t\tv1y = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				t\tv1z = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				s\shiftx = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))

				t\tv2x = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+5," "))
				t\tv2y = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				t\tv2z = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				s\shifty = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))

				s\rotation = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+3," "))
				s\scalex = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				s\scaley = Float(Explode(File_Line$,LAST_EXPLODE_CHAR%+1," "))
				
				Temp_Imagem = LoadImage(TextureRoot$+s\texture$+".bmp")
				If Temp_Imagem = 0 Then RuntimeError ("Texture "+TextureRoot$+s\texture$+".bmp not found!")
				s\IW = ImageWidth(Temp_Imagem)
				s\IH = ImageHeight(Temp_Imagem)
				FreeImage Temp_Imagem:Temp_Imagem = 0
				
				t\On_File% = 1
			ElseIf Left(File_Line$,1) = "}" Then 
				Brack_Open = Brack_Open - 1 ;Fecha chaves
			EndIf
		EndIf
		If Len(Trim(File_Line$)) > 0 Then File_Line_Last$ = File_Line$
	Wend
	CloseFile Map_File
	If Brack_Open <> 0 Then RuntimeError("Some bracket was missmatch")

	;Calcula coordenadas UV
	For t.triangle = Each triangle
		If t\on_File = 1
			s.surface = Find_Surface(t\surf_num)
	
			t\u1 = ((t\x1 * t\tv1x + t\y1 * t\tv1z + t\z1 * t\tv1y)/s\scalex + s\shiftx)/s\IW
			t\u2 = ((t\x2 * t\tv1x + t\y2 * t\tv1z + t\z2 * t\tv1y)/s\scalex + s\shiftx)/s\IW
			t\u3 = ((t\x3 * t\tv1x + t\y3 * t\tv1z + t\z3 * t\tv1y)/s\scalex + s\shiftx)/s\IW

			t\v1 = ((t\x1 * t\tv2x + t\y1 * t\tv2z + t\z1 * t\tv2y)/s\scaley + s\shifty)/s\IH
			t\v2 = ((t\x2 * t\tv2x + t\y2 * t\tv2z + t\z2 * t\tv2y)/s\scaley + s\shifty)/s\IH
			t\v3 = ((t\x3 * t\tv2x + t\y3 * t\tv2z + t\z3 * t\tv2y)/s\scaley + s\shifty)/s\IH
		EndIf
	Next
	
	;Completa Surfaces
	For t.triangle = Each triangle
		If t\On_File = 1 And Only_File = 0

			mA# = t\y1*(t\z2 - t\z3) + t\y2*(t\z3 - t\z1) + t\y3*(t\z1 - t\z2)
			mB# = t\z1*(t\x2 - t\x3) + t\z2*(t\x3 - t\x1) + t\z3*(t\x1 - t\x2)
			mC# = t\x1*(t\y2 - t\y3) + t\x2*(t\y3 - t\y1) + t\x3*(t\y1 - t\y2)
			mD# = (t\x1*((t\y2*t\z3) - (t\y3*t\z2)) + t\x2*((t\y3*t\z1) - (t\y1*t\z3)) + t\x3*((t\y1*t\z2) - (t\y2*t\z1)))
				
			If ma = 0 And mb = 0 And mc = 0 Then RuntimeError("equation 0")
								
			Found = 0
			For t2.triangle = Each triangle
				If t2\Mesh_Num = t\Mesh_Num And t\Num <> t2\Num

					;Equation 1
					mA_Res1# = mA * t2\x1
					mB_Res1# = mB * t2\y1
					mC_Res1# = mC * t2\z1
					;Equation 2
					mA_Res2# = mA * t2\x2
					mB_Res2# = mB * t2\y2
					mC_Res2# = mC * t2\z2
					;Equation 3
					mA_Res3# = mA * t2\x3
					mB_Res3# = mB * t2\y3
					mC_Res3# = mC * t2\z3
													
					If (mA_Res1 + mB_Res1 + mC_Res1) = mD Then
						If Not (((t\x1 = t2\x1) And (t\y1 = t2\y1) And (t\z1 = t2\z1)) Or ((t\x2 = t2\x1) And (t\y2 = t2\y1) And (t\z2 = t2\z1))  Or ((t\x3 = t2\x1) And (t\y3 = t2\y1) And (t\z3 = t2\z1))) Then 
							If Not Triangle_Exists% (t\x1,t\y1,t\z1,t\x3,t\y3,t\z3,t2\x1,t2\y1,t2\z1,t\mesh_num%)
								Found = 1
								x4 = t2\x1
								y4 = t2\y1
								z4 = t2\z1
								Goto Quit_Loop
							EndIf
						EndIf
					EndIf

					If (mA_Res2 + mB_Res2 + mC_Res2) = mD Then
						If Not (((t\x1 = t2\x2) And (t\y1 = t2\y2) And (t\z1 = t2\z2)) Or ((t\x2 = t2\x2) And (t\y2 = t2\y2) And (t\z2 = t2\z2))  Or ((t\x3 = t2\x2) And (t\y3 = t2\y2) And (t\z3 = t2\z2))) Then 
							If Not Triangle_Exists% (t\x1,t\y1,t\z1,t\x3,t\y3,t\z3,t2\x2,t2\y2,t2\z2,t\mesh_num%)
								Found = 1
								x4 = t2\x2
								y4 = t2\y2
								z4 = t2\z2
								Goto Quit_Loop
							EndIf
						EndIf
					EndIf

					If (mA_Res3 + mB_Res3 + mC_Res3) = mD Then
						If Not (((t\x1 = t2\x3) And (t\y1 = t2\y3) And (t\z1 = t2\z3)) Or ((t\x2 = t2\x3) And (t\y2 = t2\y3) And (t\z2 = t2\z3))  Or ((t\x3 = t2\x3) And (t\y3 = t2\y3) And (t\z3 = t2\z3))) Then 
							If Not Triangle_Exists% (t\x1,t\y1,t\z1,t\x3,t\y3,t\z3,t2\x3,t2\y3,t2\z3,t\mesh_num%)
								Found = 1
								x4 = t2\x3
								y4 = t2\y3
								z4 = t2\z3
								Goto Quit_Loop
							EndIf
						EndIf
					EndIf
				EndIf
			Next
			.Quit_Loop
			
			If Found = 1
				;Cria Triangulo
					t2.Triangle = New Triangle
					t2\num = TRIANGLE_COUNT%
					t2\surf_num = t\surf_num
					t2\mesh_num% = t\mesh_num%
					TRIANGLE_COUNT% = TRIANGLE_COUNT% + 1		
									
					;Brush
					t2\x1# = t\x3
					t2\y1# = t\y3
					t2\z1# = t\z3
					t2\x2# = x4
					t2\y2# = y4
					t2\z2# = z4
					t2\x3# = t\x1
					t2\y3# = t\y1
					t2\z3# = t\z1

					;Coordenadas do vetor de textura
					t2\tv1x = t\tv1x
					t2\tv1y = t\tv1y
					t2\tv1z = t\tv1z
					t2\tv2x = t\tv2x
					t2\tv2y = t\tv2y
					t2\tv2z = t\tv2z
					
					s.surface = Find_Surface (t2\surf_num)
									
					;Texture
					t2\u1 = ((t2\x1 * t2\tv1x + t2\y1 * t2\tv1z + t2\z1 * t2\tv1y)/s\scalex + s\shiftx)/s\IW
					t2\u2 = ((t2\x2 * t2\tv1x + t2\y2 * t2\tv1z + t2\z2 * t2\tv1y)/s\scalex + s\shiftx)/s\IW
					t2\u3 = ((t2\x3 * t2\tv1x + t2\y3 * t2\tv1z + t2\z3 * t2\tv1y)/s\scalex + s\shiftx)/s\IW
					
					t2\v1 = ((t2\x1 * t2\tv2x + t2\y1 * t2\tv2z + t2\z1 * t2\tv2y)/s\scaley + s\shifty)/s\IH
					t2\v2 = ((t2\x2 * t2\tv2x + t2\y2 * t2\tv2z + t2\z2 * t2\tv2y)/s\scaley + s\shifty)/s\IH
					t2\v3 = ((t2\x3 * t2\tv2x + t2\y3 * t2\tv2z + t2\z3 * t2\tv2y)/s\scaley + s\shifty)/s\IH
					
					t2\On_file = 0
			EndIf
		EndIf
	Next
End Function

;FIM CARREGA MAPA


;CONSTROI MAPA
Function PXWC_BuildMap()
	;Cria Meshes
	For m.mesh = Each mesh
		m\hand = CreateMesh()
		mesh_x# = 0
		mesh_y# = 0
		mesh_z# = 0
		mesh_counter# = 0
		For t.triangle = Each triangle
			If t\On_File = 1 And t\Mesh_num = m\num Then
				mesh_x# = mesh_x + t\x1 + t\x2 + t\x3
				mesh_y# = mesh_y + t\y1 + t\y2 + t\y3
				mesh_z# = mesh_z + t\z1 + t\z2 + t\z3
				mesh_counter# = mesh_counter + 3
			EndIf
		Next
		mesh_x = mesh_x/mesh_counter
		mesh_y = mesh_y/mesh_counter
		mesh_z = mesh_z/mesh_counter
		PositionEntity m\hand,mesh_x/MAP_SCALE,mesh_y/MAP_SCALE,mesh_z/MAP_SCALE,0
	Next

	;Cria Superficies
	For s.surface = Each surface
		m.mesh = Find_Mesh(s\mesh_num%)
		s\hand = CreateSurface(m\hand)
		
		TBlend = GetProperty$(m\entity, "blend"):If TBlend = 0 Then TBlend = 2 ;Blending Mode das texturas
				
		If FLAT_TEXTURE = 0
			x.texture  = New texture
			x\num = TEXTURE_COUNT%
			TEXTURE_COUNT% = TEXTURE_COUNT% + 1
			x\surf_num = s\num%
			x\name     = s\texture$
			x\shiftx   = s\shiftx
			x\shifty   = s\shifty
			x\scalex   = s\scalex
			x\scaley   = s\scaley
			x\rotation = s\rotation
			x\hand = LoadTexture(TextureRoot$+x\name+".bmp")
						
			x\brush = CreateBrush()
			BrushTexture x\brush,x\hand,0,0 ;Layer 0
			If FileType("shadow/"+s\num+".bmp") = 1 Then ApplyShading(x\brush,s\num)			
			x\decal_layer = CreateDecalLayer(x\brush, s\num, s\IW, s\IH)
			
			PaintSurface s\hand,x\brush
		EndIf
	Next

	;Cria Triangulos
	For t.triangle = Each triangle
		If RENDER_FILE_ONLY = 0 Or t\On_File = 1
			m.mesh = Find_Mesh(t\Mesh_num)
			mx = EntityX(m\hand)*MAP_SCALE
			my = EntityY(m\hand)*MAP_SCALE
			mz = EntityZ(m\hand)*MAP_SCALE
			s.surface = Find_Surface(t\surf_num)
			v0 = AddVertex(s\hand,(t\x1-mx)/MAP_SCALE,(t\y1-my)/MAP_SCALE,(t\z1-mz)/MAP_SCALE,t\u1,t\v1)
			v1 = AddVertex(s\hand,(t\x2-mx)/MAP_SCALE,(t\y2-my)/MAP_SCALE,(t\z2-mz)/MAP_SCALE,t\u2,t\v2)
			v2 = AddVertex(s\hand,(t\x3-mx)/MAP_SCALE,(t\y3-my)/MAP_SCALE,(t\z3-mz)/MAP_SCALE,t\u3,t\v3)
			t\hand = AddTriangle (s\hand,v0,v1,v2)
			If Both_Sides Then AddTriangle (s\hand,v2,v1,v0) ;DEBUG

		EndIf
	Next

	;Corrige Meshes e coloca textura FLAT
	For m.mesh = Each mesh
		;RotateEntity m\hand,90,0,0,0
		If FLAT_TEXTURE = 1
			If GetProperty(m\entity,"classname") = "sky" ;Entidade Sky tem cor especial
				brush = CreateBrush(158,205,255)
			Else
				brush = CreateBrush(Rand(80,230),Rand(80,230),Rand(80,230))
			EndIf
				PaintMesh m\hand,brush
		EndIf
	Next
End Function
;FIM CONSTROI MAPA

Function Substr$(St$, Sta, Le)
	Return Left(Right(St$, Len(St$)-Sta),Le)
End Function

Function Explode$(St$, Sta, Sep$)
	char = Sta
	val$ = ""
	While (Substr(St$,char,1) <> Sep$)
		val$ = val$ + Substr(St$,char,1)
		char = char + 1
	Wend
	LAST_EXPLODE_CHAR% = char
	Return val$
End Function

Function Find_Mesh.Mesh (num%)
	For m.mesh = Each mesh
		If m\num = num Then Return m
	Next
End Function

Function Find_Entity.Entity (num%)
	For e.entity = Each entity
		If e\num = num Then Return e
	Next
End Function

Function Find_MeshByEntity.Mesh (num%, offset=0)
	For m.mesh = Each mesh
		If m\entity = num Then
			If offset <= 0 Then 
				Return m
			Else
				offset = offset - 1
			EndIf
		EndIf
	Next
End Function

Function Find_SurfaceByMesh.Surface (num%, offset=0)
	For s.surface = Each surface
		If s\mesh_num = num Then
			If offset <= 0 Then 
				Return s
			Else
				offset = offset - 1
			EndIf
		EndIf
	Next
End Function

Function Find_SurfaceByHandle.Surface (num%, offset=0)
	For s.surface = Each surface
		If s\hand = num Then
			Return s
		EndIf
	Next
End Function

Function Find_TriangleBySurface.Triangle (num%, offset=0)
	For t.triangle = Each triangle
		If t\surf_num = num Then
			If offset <= 0 Then 
				Return t
			Else
				offset = offset - 1
			EndIf
		EndIf
	Next
End Function

Function Find_TextureBySurface.Texture (num%)
	For x.texture = Each texture
		If x\surf_num = num Then
			Return x
		EndIf
	Next
End Function

Function Triangle_Exists% (x1,y1,z1,x2,y2,z2,x3,y3,z3,mesh%)
	For t.triangle = Each triangle
		If ((t\x1=x1) And (t\y1=y1) And (t\z1=z1))
			If ((t\x2=x2) And (t\y2=y2) And (t\z2=z2))
				If ((t\x2=x2) And (t\y2=y2) And (t\z2=z2)) And t\mesh_num=mesh
					Return 1
				EndIf
			EndIf
		EndIf
	Next
End Function

Function Count_MeshByEntity (num%)
	a = 0
	For m.mesh = Each mesh
		If m\entity = num Then a = a + 1
	Next
	Return a
End Function

Function Count_SurfaceByMesh (num%)
	a = 0
	For s.surface = Each surface
		If s\mesh_num = num Then a = a + 1
	Next
	Return a
End Function

Function Count_TriangleBySurface (num%)
	a = 0
	For t.triangle = Each triangle
		If t\surf_num = num Then a = a + 1
	Next
	Return a
End Function

Function Find_Surface.Surface (num%)
	For s.surface = Each surface
		If s\num = num Then Return s
	Next
End Function

Function Find_Texture.texture(name$, shiftx, shifty, scalex, scaley, rotation)
	For x.texture = Each texture
		If x\name = name And x\shiftx = shiftx And x\shifty=shifty And x\scalex=scalex And x\scaley=scaley And x\rotation = rotation
			Return x
		EndIf
	Next
	Return Null
End Function

Function Find_Texture_ByName.texture(name$)
	For x.texture = Each texture
		If x\name = name
			Return x
		EndIf
	Next
	Return Null
End Function

Function GetProperty$(entity%, name$)
	For p.property = Each property
		If p\entity_num = entity% And p\name$ = name$ Then Return p\value$
	Next
	Return ""
End Function

Function Find_EntityByClassname%(class$, offset%=0)
	For p.property = Each property
		If p\name$ = "classname" And p\value$ = class$
			If offset = 0
				Return p\entity_num%
			Else
				offset = offset - 1
			EndIf
		EndIf
	Next
	Return -1
End Function

Function SetProperty(EntityNum%, Name$, Value$)
	Found = 0
	For p.property = Each property
		If p\entity_num = EntityNum% And p\name$ = name$ Then found=1:Exit
	Next
	If found = 1
		p\value$ = value$
	Else
		p = New property
		p\entity_num = entitynum
		p\name$ = name$
		p\value$ = value$
	EndIf
End Function

Function Calculate3DDistance#(x1,y1,z1,x2,y2,z2) ;Não utilizada
	Return Sqr(((Sqr(((x2 - x1)^2) + ((y2 - y1)^2)))^2) + ((z2 - z1)^2))
End Function

Function ReCreateMesh%(mesh_num%) ;NOT TESTED!
	m.mesh = Find_Mesh(mesh_num)
	m\hand = CreateMesh()
	For t.triangle = Each triangle
			If t\On_File = 1 And t\Mesh_num = m\num Then PositionEntity m\hand,t\x1/MAP_SCALE,t\y1/MAP_SCALE,t\z1/MAP_SCALE,0
	Next
	NM% = Count_SurfaceByMesh (mesh_num%)
	For a = 0 To NM-1
		s.surface = Find_SurfaceByMesh.Surface (mesh_num,a)
		s\hand = CreateSurface(m\hand)
		x.texture = Find_TextureBySurface(s\num)
		PaintSurface s\hand,x\brush
		
		NM2% = Count_TriangleBySurface (s\num%)
		For c = 0 To NM2-1
			t.triangle = Find_TriangleBySurface.Triangle (s\num%,c)
			mx = EntityX(m\hand)*MAP_SCALE
			my = EntityY(m\hand)*MAP_SCALE
			mz = EntityZ(m\hand)*MAP_SCALE
			v0 = AddVertex(s\hand,(t\x1-mx)/MAP_SCALE,(t\y1-my)/MAP_SCALE,(t\z1-mz)/MAP_SCALE,t\u1,t\v1)
			v1 = AddVertex(s\hand,(t\x2-mx)/MAP_SCALE,(t\y2-my)/MAP_SCALE,(t\z2-mz)/MAP_SCALE,t\u2,t\v2)
			v2 = AddVertex(s\hand,(t\x3-mx)/MAP_SCALE,(t\y3-my)/MAP_SCALE,(t\z3-mz)/MAP_SCALE,t\u3,t\v3)
			t\hand = AddTriangle (s\hand,v0,v1,v2)
		Next
	Next
End Function