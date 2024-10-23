import * as THREE from 'https://unpkg.com/three@0.126.1/build/three.module.js'
import { OrbitControls } from 'https://unpkg.com/three@0.126.1/examples/jsm/controls/OrbitControls.js'
import gsap from "https://cdn.skypack.dev/gsap"
import * as dat from "https://cdn.skypack.dev/dat.gui"

const gui = new dat.GUI()
gui.close();

const world = {
  plane: {
    width: 500,
    height: 500,
    widthSegments: 70,
    heightSegments: 70
  },
  ripple: {
    rippleMaxRadius: 20,
    rippleAmplitude: 3,
    rippleFrequency: 0.05    
  },
  orbitControl: {
    enableRotate: false,
    enableZoom: false,
    enablePan: false
  }
}
gui.add(world.plane, 'width', 1, 700).onChange(generatePlane)
gui.add(world.plane, 'height', 1, 700).onChange(generatePlane)
gui.add(world.plane, 'widthSegments', 1, 100).onChange(generatePlane)
gui.add(world.plane, 'heightSegments', 1, 100).onChange(generatePlane)

gui.add(world.ripple, 'rippleMaxRadius', 1, 100).onChange(updateRippleParams)
gui.add(world.ripple, 'rippleAmplitude', 0.1, 10).onChange(updateRippleParams)
gui.add(world.ripple, 'rippleFrequency', 0.01, 1).onChange(updateRippleParams)

// Add GUI controls for OrbitControls toggles
/*gui.add(world.orbitControl, 'enableRotate').onChange((value) => {controls.enableRotate = value;});
gui.add(world.orbitControl, 'enableZoom').onChange((value) => {controls.enableZoom = value;});
gui.add(world.orbitControl, 'enablePan').onChange((value) => {controls.enablePan = value;});*/

function updateRippleParams() {
  rippleMaxRadius = world.ripple.rippleMaxRadius
  rippleAmplitude = world.ripple.rippleAmplitude
  rippleFrequency = world.ripple.rippleFrequency
}

function generatePlane() {
  planeMesh.geometry.dispose()
  planeMesh.geometry = new THREE.PlaneGeometry(
    world.plane.width,
    world.plane.height,
    world.plane.widthSegments,
    world.plane.heightSegments
  )

  // vertice position randomization
  const { array } = planeMesh.geometry.attributes.position
  const randomValues = []
  for (let i = 0; i < array.length; i++) {
    if (i % 3 === 0) {
      const x = array[i]
      const y = array[i + 1]
      const z = array[i + 2]

      array[i] = x + (Math.random() - 0.5) * 3
      array[i + 1] = y + (Math.random() - 0.5) * 3
      array[i + 2] = z + (Math.random() - 0.5) * 3
    }

    randomValues.push(Math.random() * Math.PI * 2)
  }

  planeMesh.geometry.attributes.position.randomValues = randomValues
  planeMesh.geometry.attributes.position.originalPosition =
    planeMesh.geometry.attributes.position.array

  const colors = []
  for (let i = 0; i < planeMesh.geometry.attributes.position.count; i++) {
    colors.push(0.25, 0, 0.35)
  }

  planeMesh.geometry.setAttribute(
    'color',
    new THREE.BufferAttribute(new Float32Array(colors), 3)
  )
}

const raycaster = new THREE.Raycaster()
const scene = new THREE.Scene()
const camera = new THREE.PerspectiveCamera(
  75,
  innerWidth / innerHeight,
  0.1,
  1000
)

const canvas = document.getElementById('backgroundCanvas')
const renderer = new THREE.WebGLRenderer({canvas})

renderer.setSize(window.innerWidth, window.innerHeight)
renderer.setPixelRatio(window.devicePixelRatio)
document.body.appendChild(renderer.domElement)

const controls = new OrbitControls(camera, renderer.domElement)
controls.enableRotate = false
controls.enableZoom = false
controls.enablePan = false 
camera.position.y = 40
camera.position.z = 50
controls.update();

const planeGeometry = new THREE.PlaneGeometry(
  world.plane.width,
  world.plane.height,
  world.plane.widthSegments,
  world.plane.heightSegments
)
const planeMaterial = new THREE.MeshPhongMaterial({
  side: THREE.DoubleSide,
  flatShading: THREE.FlatShading,
  vertexColors: true
})
const planeMesh = new THREE.Mesh(planeGeometry, planeMaterial)
scene.add(planeMesh)
generatePlane()

const light = new THREE.DirectionalLight(0xffffff, 1)
light.position.set(0, -1, 1)
scene.add(light)

const backLight = new THREE.DirectionalLight(0xffffff, 1)
backLight.position.set(0, 0, -1)
scene.add(backLight)

const mouse = {
  x: undefined,
  y: undefined
}

let frame = 0
let rippleOrigin = { x: 0, z: 0 }
let rippleActive = false
let rippleTime = 0
let rippleMaxRadius = world.ripple.rippleMaxRadius
let rippleAmplitude = world.ripple.rippleAmplitude
let rippleFrequency = world.ripple.rippleFrequency
let ripple = []

function damping(distance) {
  return Math.max(1 - distance/rippleMaxRadius, 0)
}

function getRippleDisplacement(x, y, rippleTime) {
  const distanceFromClick = Math.sqrt((x - rippleOrigin.x)**2 + (y - rippleOrigin.z)**2);
  if(distanceFromClick < rippleMaxRadius) {
    const waveFront = rippleTime - distanceFromClick * rippleFrequency * 20;
    if(waveFront > 0 && waveFront < Math.PI*2) {
      const dampenedAmplitude = damping(distanceFromClick) * rippleAmplitude;
      return Math.sin(waveFront) * dampenedAmplitude;
    }
  }
  return 0;
}

function animate() {
  requestAnimationFrame(animate)
  renderer.render(scene, camera)
  raycaster.setFromCamera(mouse, camera)
  frame += 0.01
  if(rippleActive) {
    rippleTime += 0.5;
    console.log(`Ripple Time: ${rippleTime}, Ripple Active: ${rippleActive}`);
  }
  
  const {
    array,
    originalPosition,
    randomValues
  } = planeMesh.geometry.attributes.position
  for (let i = 0; i < array.length; i += 3) {
    const randomDisplacementX = Math.cos(frame + randomValues[i]) * 0.02;
    const randomDisplacementY = Math.sin(frame + randomValues[i + 1]) * 0.002;
    const randomDisplacementZ = Math.cos(frame + randomValues[i + 2] * 0.01);
    const x = originalPosition[i]
    const y = originalPosition[i + 1]
    const z = originalPosition[i + 2]
    
    const rippleDisplacementZ = rippleActive? getRippleDisplacement(x, y, rippleTime) : 0;
    // const finalDisplacementY = randomDisplacementY + rippleDisplacement;
    // x
    array[i] = x + randomDisplacementX;
    // y
    array[i + 1] = y + randomDisplacementY; 
    // Z
    array[i + 2] = z + rippleDisplacementZ;
  }

  planeMesh.geometry.attributes.position.needsUpdate = true
  // Stop ripple after it passes max radius
  if (rippleTime > rippleMaxRadius / rippleFrequency)   {
    rippleActive = false
    rippleTime = 0
  }
  
  const intersects = raycaster.intersectObject(planeMesh)
  if (intersects.length > 0) {
    const { color } = intersects[0].object.geometry.attributes

    // vertice 1
    color.setX(intersects[0].face.a, 0.8)
    color.setY(intersects[0].face.a, 0.2)
    color.setZ(intersects[0].face.a, 1)

    // vertice 2
    color.setX(intersects[0].face.b, 0.8)
    color.setY(intersects[0].face.b, 0.2)
    color.setZ(intersects[0].face.b, 1)

    // vertice 3
    color.setX(intersects[0].face.c, 0.8)
    color.setY(intersects[0].face.c, 0.2)
    color.setZ(intersects[0].face.c, 1)

    intersects[0].object.geometry.attributes.color.needsUpdate = true

    const initialColor = {
      r: 0.25,
      g: 0,
      b: 0.35
    }

    const hoverColor = {
      r: 0.8,
      g: 0.2,
      b: 1
    }

    gsap.to(hoverColor, {
      r: initialColor.r,
      g: initialColor.g,
      b: initialColor.b,
      duration: 1,
      onUpdate: () => {
        // vertice 1
        color.setX(intersects[0].face.a, hoverColor.r)
        color.setY(intersects[0].face.a, hoverColor.g)
        color.setZ(intersects[0].face.a, hoverColor.b)

        // vertice 2
        color.setX(intersects[0].face.b, hoverColor.r)
        color.setY(intersects[0].face.b, hoverColor.g)
        color.setZ(intersects[0].face.b, hoverColor.b)

        // vertice 3
        color.setX(intersects[0].face.c, hoverColor.r)
        color.setY(intersects[0].face.c, hoverColor.g)
        color.setZ(intersects[0].face.c, hoverColor.b)
        color.needsUpdate = true
      }
    })
  }
}

animate()

// Mouse click to start ripple
addEventListener('click', (event) => {
  mouse.x = (event.clientX / innerWidth) * 2 - 1;
  mouse.y = -(event.clientY / innerHeight) * 2 + 1;
  raycaster.setFromCamera(mouse, camera);

  const intersects = raycaster.intersectObject(planeMesh);
  if (intersects.length > 0) {
    const { point } = intersects[0];
    rippleOrigin = { x: point.x, z: point.y };
    rippleActive = true;
    rippleTime = 0;
    console.log('Ripple Origin:', rippleOrigin);
  }
});

addEventListener('mousemove', (event) => {
  mouse.x = (event.clientX / innerWidth) * 2 - 1
  mouse.y = -(event.clientY / innerHeight) * 2 + 1
})