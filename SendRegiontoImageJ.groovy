def viewer = getCurrentViewer().getDisplayedRegionShape()
def plane = getCurrentViewer().getImagePlane()
def photo_region = viewer.getBounds()
def photo_width = photo_region.getWidth()
def photo_height = photo_region.getHeight()
def photoX = photo_region.getX()
def photoY = photo_region.getY()
def roi = ROIs.createRectangleROI(photoX, photoY, photo_width, photo_height, plane)
def annotation = PathObjects.createAnnotationObject(roi)
addObject(annotation)

// Estimate the downscale factor to achieve a target file size
double targetFileSizeMB = 100.0
double bytesPerPixel = 4 // Assuming 24-bit RGB image, 3 bytes per pixel
double targetFileSizeBytes = targetFileSizeMB * (1024 * 1024)

double currentDownscaleFactor = 0.1

def estimateFileSize = { double downscaleFactor ->
    double downscaleWidth = photo_width / (downscaleFactor)
    double downscaleHeight = photo_height / (downscaleFactor)
    print((downscaleWidth * downscaleHeight * bytesPerPixel))
    print downscaleFactor
    return ((downscaleWidth * downscaleHeight) * bytesPerPixel) // Convert to MB
}

print estimateFileSize(currentDownscaleFactor)

// Adjust downscale factor to get close to the target file size
while (estimateFileSize(currentDownscaleFactor) > targetFileSizeBytes && currentDownscaleFactor > 0.01) {
    currentDownscaleFactor += 0.01
}

def server = getCurrentServer()
def request = RegionRequest.createInstance(server.getPath(), currentDownscaleFactor, roi)
def pathImage = IJTools.convertToImagePlus(server, request)
def imp = pathImage.getImage()
imp.show()

// Convert QuPath ROI to ImageJ Roi & add to open image
def roiIJ = IJTools.convertToIJRoi(roi, pathImage)
imp.setRoi(roiIJ)

// Optional: Print the final downscale factor
print "Final downscale factor: " + currentDownscaleFactor